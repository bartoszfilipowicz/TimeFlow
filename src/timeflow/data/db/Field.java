package timeflow.data.db;


import java.util.Objects;

public class Field
{
	private String name;
	private Class type;
	int index;
	private int recommendedSize = -1;

	public Field(String name, Class type)
	{
		this.name=name;
		this.type=type;
	}

	public Field(String name, Class type, int recommendedSize)
	{
		this.name=name;
		this.type=type;
		this.recommendedSize=recommendedSize;
	}

	public int getRecommendedSize()
    {
		return recommendedSize;
	}

	public void setRecommendedSize(int recommendedSize)
    {
		this.recommendedSize = recommendedSize;
	}

	void setName(String name)
	{
		this.name=name;
	}

	public String getName()
	{
		return name;
	}

	public Class getType()
	{
		return type;
	}

    @Override
    public int hashCode()
    {
        return Objects.hash(name, type);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Field))
        {
            return false;
        }

        Field other = (Field) obj;

        return
            Objects.equals(name, other.name) &&
            Objects.equals(type, other.type);
    }

    @Override
	public String toString()
	{
		return "[Field: name='"+name+"', type="+type+", index="+index+"]";
	}
}
