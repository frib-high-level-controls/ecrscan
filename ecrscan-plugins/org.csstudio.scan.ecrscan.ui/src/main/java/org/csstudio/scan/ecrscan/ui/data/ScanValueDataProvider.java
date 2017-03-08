package org.csstudio.scan.ecrscan.ui.data;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.csstudio.javafx.rtplot.data.PlotDataItem;
import org.csstudio.javafx.rtplot.data.PlotDataProvider;
import org.csstudio.javafx.rtplot.data.SimpleDataItem;
import org.diirt.util.array.ArrayDouble;
import org.diirt.util.array.IteratorNumber;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VTable;
import org.diirt.vtype.table.VTableFactory;
import org.ejml.data.DenseMatrix64F;
import org.ejml.equation.Equation;
import org.ejml.equation.Sequence;

public class ScanValueDataProvider implements PlotDataProvider<Double> {
    final private ReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile ListNumber x_data, y_data;
    private volatile int size = 0;
    private final Equation xequation = new Equation();
    private final Equation yequation = new Equation();
    private final String xformulaString;
    private final String yformulaString;
    private  Sequence xformula;
    private  Sequence yformula;
    DenseMatrix64F x = new DenseMatrix64F(1,1);  // gets resized
    DenseMatrix64F y = new DenseMatrix64F(1,1);  // gets resized
    
    public ScanValueDataProvider(String xformula, String yformula){
    	this.xformulaString = xformula;
    	this.yformulaString = yformula;
        xequation.alias(x, "x");
        yequation.alias(y, "y");
    }
    
    /** {@inheritDoc} */
    @Override
    public Lock getLock(){
        return lock.readLock();
    }
    
    private static final class IteratorNumberDouble implements Iterator<Double> {
        protected IteratorNumber iteratorNumber;
        public IteratorNumberDouble(IteratorNumber iteratorNumber) {this.iteratorNumber = iteratorNumber;}
        public boolean hasNext() {return iteratorNumber.hasNext();}
        public Double next() {return iteratorNumber.nextDouble();}
        public void remove() {throw new UnsupportedOperationException(); }
    }

    /** Update the waveform value.
     *  @param value New value
     *  Fires event to listeners (plot)
     */
    public void setValue(final VTable value) {
        if (value==null) return;
        
        List<String> columnNames = VTableFactory.columnNames(value);
        String newxformulaString = xformulaString;
        String newyformulaString = yformulaString;
        for (String columnName: columnNames){
        	ListNumber new_x;
            int xcolumnIndex = columnNames.indexOf(columnName);
            Object columnValue = value.getColumnData(xcolumnIndex);
            if (columnValue instanceof VNumberArray) {
                new_x = ((VNumberArray) columnValue).getData();
            } else if (columnValue instanceof ListNumber){
                new_x = (ListNumber)columnValue;
            } else if (columnValue instanceof List) {
                // this shouldn't enter
            	continue;
                // I'm not sure how to handle timestamps yet
            	//new_x = new ArrayDouble(((List<Double>)columnValue).stream().mapToDouble(Double::valueOf).toArray());
            } else {
                continue;
            }
            Iterator<Double> iteratorNumberDouble = new IteratorNumberDouble(new_x.iterator());
            Iterable<Double> iterable = () -> iteratorNumberDouble;
            Stream<Double> targetStream = StreamSupport.stream(iterable.spliterator(), false);
            double[] data = targetStream.mapToDouble(d -> d.doubleValue()).toArray();
            DenseMatrix64F denseMatrix64F = new DenseMatrix64F(new_x.size(),1,true,data);
            xequation.alias(denseMatrix64F, "x"+String.valueOf(xcolumnIndex));
            yequation.alias(denseMatrix64F, "x"+String.valueOf(xcolumnIndex));
            newxformulaString = newxformulaString.replace(columnName, "x"+String.valueOf(xcolumnIndex));
            newyformulaString = newyformulaString.replace(columnName, "x"+String.valueOf(xcolumnIndex));
        }
        xequation.process("x = "+newxformulaString);
        yequation.process("y = "+newyformulaString);

        lock.writeLock().lock();
        try{
            x_data = new ArrayDouble(x.data);
            y_data = new ArrayDouble(y.data);
            size = y_data == null ? x_data.size() : Math.min(x_data.size(), y_data.size());
        }
        finally{
            lock.writeLock().unlock();
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