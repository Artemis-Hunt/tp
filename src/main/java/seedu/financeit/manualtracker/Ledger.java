package seedu.financeit.manualtracker;

import seedu.financeit.common.CommandPacket;
import seedu.financeit.common.Constants;
import seedu.financeit.common.DateTimeItem;
import seedu.financeit.common.exceptions.InsufficientParamsException;
import seedu.financeit.common.exceptions.ItemNotFoundException;
import seedu.financeit.common.exceptions.ParseFailParamException;
import seedu.financeit.manualtracker.subroutine.EntryList;
import seedu.financeit.ui.UiManager;
import seedu.financeit.utils.ParamChecker;

import java.time.LocalDate;

public class Ledger extends DateTimeItem {
    protected LocalDate date = null;
    public EntryList entryList = new EntryList(this);
    private ParamChecker paramChecker;

    public Ledger() {
        super();
        super.setDefaultDateTimeFormat("date");
    }

    public void handlePacket(CommandPacket packet) throws InsufficientParamsException {
        this.paramChecker = new ParamChecker(packet);
        try {
            this.handleParams(packet);
        } catch (ItemNotFoundException exception) {
            // Fall-through
        }
    }

    @Override
    public String getName() {
        return String.format("Ledger %d : [ %s ]", this.index + 1,
            this.dateTimeOutputManager.getSingleDateFormatted("date"));
    }

    @Override
    public boolean equals(Object object) {
        Ledger entry = (Ledger) object;
        return (this.date.equals(entry.date));
    }

    @Override
    public String toString() {
        return super.getDateFormatted();
    }

    @Override
    public void handleSingleParam(CommandPacket packet, String paramType) throws ParseFailParamException {
        switch (paramType) {
        case ParamChecker.PARAM_DATE:
            date = paramChecker.checkAndReturnDate(paramType);
            this.setDate(date);
            break;

        default:
            if (!super.requiredParams.contains(paramType)) {
                UiManager.printWithStatusIcon(Constants.PrintType.ERROR_MESSAGE,
                    paramChecker.getUnrecognizedParamMessage(paramType));
            }
            break;
        }
    }
}