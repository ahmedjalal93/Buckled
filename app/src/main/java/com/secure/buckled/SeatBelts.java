package com.secure.buckled;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;

public class SeatBelts {
    public static final String SEATBELTSTATES = "seatBeltsStates";

    public static int seatState1 = -1;
    public static int seatState2 = -1;
    public enum SeatBelt{

        //VMCERUIOPQ
        UNKNOWN(0),
        SEATBELT1(KeyEvent.KEYCODE_V),
        SEATBELT2(KeyEvent.KEYCODE_M),
        SEATBELT3(KeyEvent.KEYCODE_C),
        SEATBELT4(KeyEvent.KEYCODE_E),
        SEATBELT5(KeyEvent.KEYCODE_R),
        SEATBELT6(KeyEvent.KEYCODE_U),
        SEATBELT7(KeyEvent.KEYCODE_I),
        SEATBELT8(KeyEvent.KEYCODE_O),
        SEATBELT9(KeyEvent.KEYCODE_P),
        SEATBELT10(KeyEvent.KEYCODE_Q);

        public final int seatID;
        SeatBelt(int seatID) {
            this.seatID = seatID;
        }

        public static SeatBelt findSeatBelt(int id){
            SeatBelt[] seats = values();
            for (SeatBelt seat : seats ){
                if(seat.seatID == id){
                    return seat;
                }
            }
            return UNKNOWN;
        }
    }

    public enum SeatBeltState{
        UNKNOWN(-1),
        OFF(0),
        ON(1);
        public final int intValue;
        SeatBeltState(int intValue) {
            this.intValue = intValue;
        }
    }

    public static int setSeatBeltState(int seat, int state, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(SEATBELTSTATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(SeatBelts.SeatBelt.findSeatBelt(seat).name(), state);
        editor.commit();
        return state;
    }

    public static int getSeatBeltState(int seat, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(SEATBELTSTATES, Context.MODE_PRIVATE);
        return sharedPref.getInt(SeatBelts.SeatBelt.findSeatBelt(seat).name(), -1);
    }

    public static void resetSeatBeltState(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(SEATBELTSTATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }

}
