package timeflow.app.actions;

import java.awt.event.ActionEvent;

import timeflow.app.TimeflowApp;
import timeflow.data.db.ActDB;
import timeflow.data.db.BasicDB;
import timeflow.data.db.Field;

public class CopySchemaAction extends TimeflowAction
{

    public CopySchemaAction(TimeflowApp app)
    {
        super(app, "New With Same Fields", null,
              "Create a new, blank database with same fields as the current one.");
    }

    public void actionPerformed(ActionEvent e)
    {
        java.util.List<Field> fields = getModel().getDB().getFields();
        ActDB db = new BasicDB("Unspecified");
        for (Field f : fields)
        {
            db.addField(f.getName(), f.getType());
        }
        getModel().setDB(db, "[new data]", true, this);
    }
}
