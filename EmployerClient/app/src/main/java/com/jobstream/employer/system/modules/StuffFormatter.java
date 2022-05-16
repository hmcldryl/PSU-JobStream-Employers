package com.jobstream.employer.system.modules;

import com.google.firebase.Timestamp;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StuffFormatter {

    public String formatNumber(Number count) {
        char[] suffix = {' ', 'k', 'M'};
        long numValue = count.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.00").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat().format(numValue);
        }
    }

    public String formatSalary(Double salary) {
        Locale ph = new Locale("en", "PH");
        NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(ph);
        return pesoFormat.format(salary);
    }

    public String formatTimestamp(Timestamp timestamp) {
        return new PrettyTime().format(timestamp.toDate());
    }
}
