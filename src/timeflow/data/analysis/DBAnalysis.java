package timeflow.data.analysis;

import timeflow.data.db.ActList;

public interface DBAnalysis
{

    public String getName();

    ;

    public InterestLevel perform(ActList acts);

    public String[] getResultDescription();

    public enum InterestLevel
    {
        IGNORE, BORING, INTERESTING, VERY_INTERESTING
    }


}
