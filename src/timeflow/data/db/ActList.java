package timeflow.data.db;

import java.util.LinkedHashSet;
import java.util.stream.StreamSupport;

public class ActList extends LinkedHashSet<Act>
{
    private ActDB db;

    public ActList(ActDB db)
    {
        this.db = db;
    }

    public ActDB getDB()
    {
        return db;
    }

    public Act get(int index)
    {
        if (index < 0 || index > size())
        {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        return StreamSupport.stream(spliterator(), false).skip(index).findFirst().get();
    }
}
