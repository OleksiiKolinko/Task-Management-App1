package mate.academy.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class MyDateFormatCheckValidator implements ConstraintValidator<MyDateFormatCheck, String> {
    private MyDateFormatCheck check;

    @Override
    public void initialize(MyDateFormatCheck constraintAnnotation) {
        this.check = constraintAnnotation;
    }

    @Override
    public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
        if (object == null) {
            return true;
        }
        return isValidDate(object, check.pattern());
    }

    public static boolean isValidDate(String inDate, String format) {
        if (inDate.length() != format.length()) {
            return false;
        }
        final DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }
}
