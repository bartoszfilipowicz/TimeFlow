package timeflow.app.ui.filter;

import javax.swing.JPanel;

import timeflow.data.db.filter.ActFilter;

public abstract class FilterDefinitionPanel extends JPanel
{
    public abstract ActFilter defineFilter();

    public abstract void clearFilter();
}
