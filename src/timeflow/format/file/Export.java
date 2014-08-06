package timeflow.format.file;

import java.io.BufferedWriter;

import timeflow.model.TFModel;

public interface Export
{
    public String getName();

    public void export(TFModel model, BufferedWriter out) throws Exception;
}
