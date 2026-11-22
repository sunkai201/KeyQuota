package com.chaos.keyquota;

import android.view.inputmethod.InputConnection;

/** Fill the current selection once. The host app owns its Send button. */
final class DeliveryGate {
    private long last;
    boolean fill(long seq,String text,InputConnection connection) {
        if(seq==last && seq>0)return true;
        if(seq<=last || connection==null || text.trim().isEmpty())return false;
        if(!connection.commitText(text,1))return false;
        last=seq;
        return true;
    }
}
