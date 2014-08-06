package timeflow.data.analysis;

import timeflow.data.db.ActList;
import timeflow.data.db.Field;

public interface FieldAnalysis
{

    public String getName();

    public boolean canHandleType(Class type);

    public DBAnalysis.InterestLevel perform(ActList acts, Field field);

    public String[] getResultDescription();


}
