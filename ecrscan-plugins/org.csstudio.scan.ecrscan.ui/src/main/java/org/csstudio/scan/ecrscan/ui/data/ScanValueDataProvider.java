package org.csstudio.scan.ecrscan.ui.data;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.csstudio.javafx.rtplot.data.PlotDataItem;
import org.csstudio.javafx.rtplot.data.PlotDataProvider;
import org.csstudio.javafx.rtplot.data.SimpleDataItem;
import org.diirt.util.array.ArrayDouble;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VTable;
import org.diirt.vtype.table.VTableFactory;

public class ScanValueDataProvider implements PlotDataProvider<Double> {
    final private ReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile ListNumber x_data, y_data;
    private volatile int size = 0;

    /** {@inheritDoc} */
    @Override
    public Lock getLock(){
        return lock.readLock();
    }

    /** Update the waveform value.
     *  @param value New value
     *  Fires event to listeners (plot)
     */
    public void setValue(final VTable value, String xcolumn, String ycolumn) {
        if (value==null || xcolumn == null || ycolumn == null) return;
        final ListNumber new_x, new_y;
        List<String> columnNames = VTableFactory.columnNames(value);
        int xcolumnIndex = columnNames.indexOf(xcolumn);
        if (xcolumnIndex != -1 && value.getColumnType(xcolumnIndex).equals(double.class)) {
            Object columnValue = value.getColumnData(xcolumnIndex);
            if (columnValue instanceof VNumberArray) {
                new_x = ((VNumberArray) columnValue).getData();
            } else if (columnValue instanceof ListNumber){
                new_x = (ListNumber)columnValue;
            } else if (columnValue instanceof List) {
                // this shouldn't enter
                new_x = new ArrayDouble(((List<Double>)columnValue).stream().mapToDouble(Double::valueOf).toArray());
            } else {
                return;
            }
            lock.writeLock().lock();
            try{
                x_data = new_x;
            }
            finally{
                lock.writeLock().unlock();
            }
        }
        int ycolumnIndex = columnNames.indexOf(ycolumn);
        if (ycolumnIndex != -1 && value.getColumnType(ycolumnIndex).equals(double.class)) {
            Object columnValue = value.getColumnData(ycolumnIndex);
            if (columnValue instanceof VNumberArray) {
                new_y = ((VNumberArray) columnValue).getData();
            } else if (columnValue instanceof ListNumber){
                new_y = (ListNumber)columnValue;
            } else if (columnValue instanceof List) {
                // this shouldn't enter
                new_y = new ArrayDouble(((List<Double>)columnValue).stream().mapToDouble(Double::valueOf).toArray());
            } else {
                return;
            }
            lock.writeLock().lock();
            try{
                y_data = new_y;
                size = y_data == null ? x_data.size() : Math.min(x_data.size(), y_data.size());
            }
            finally{
                lock.writeLock().unlock();
            }
        }
    }

     /** {@inheritDoc} */
    @Override
    public int size() {
        return size;
    }

    /** {@inheritDoc} */
    @Override
    public PlotDataItem<Double> get(final int index) {
        final double x = x_data == null ? index : x_data.getDouble(index);
        final double y = y_data.getDouble(index);

        return new SimpleDataItem<Double>(x, y);
    }
}