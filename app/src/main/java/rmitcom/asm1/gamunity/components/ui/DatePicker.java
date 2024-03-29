package rmitcom.asm1.gamunity.components.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DatePicker implements View.OnClickListener, DatePickerDialog.OnDateSetListener{
    EditText _editText;
    private int _day;
    private int _month;
    private int _birthYear;
    private Context _context;
    public Timestamp _timestamp;

    public DatePicker(Context context, int editTextViewID)
    {
        Activity act = (Activity)context;
        this._editText = act.findViewById(editTextViewID);
        this._editText.setOnClickListener(this);
        this._context = context;
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        _birthYear = year;
        _month = monthOfYear;
        _day = dayOfMonth;
        Calendar calendar =  new GregorianCalendar(_birthYear, _month-1, _day, 4, 0, 0);
        _timestamp = new Timestamp(calendar.getTime());
        updateDisplay();

    }
    @Override
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        DatePickerDialog dialog = new DatePickerDialog(_context, this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();

    }

    // updates the date in the birth date EditText
    private void updateDisplay() {

        _editText.setText(new StringBuilder()
                // Month is 0 based so add 1
                .append(_day).append("/").append(_month + 1).append("/").append(_birthYear).append(" "));
    }
}
