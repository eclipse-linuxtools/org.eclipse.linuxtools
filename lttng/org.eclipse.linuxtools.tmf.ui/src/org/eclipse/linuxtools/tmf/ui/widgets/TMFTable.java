package org.eclipse.linuxtools.tmf.ui.widgets;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author ematkho
 *
 */
public class TMFTable extends Composite {
	private Table table_;
	private Slider slider_;
	private int Count = 0;
	private int selected_slider;
	private int selected_table; 
	private int selected_item; 
	private int slider_max; 
	private TableItem Selected[] = null;
	private TableItem children_table_items[];
	private Control children_[]; 
	private int table_style; 
	private int number_of_items_on_table;

	/*
	 * 
	 */
	public TMFTable(Composite Parent, int Style)
	{
		super(Parent, Style);
		table_style = Style | ( SWT.NO_SCROLL ) & (~SWT.MULTI) & (~SWT.V_SCROLL) & (~SWT.VIRTUAL); 
		initialize();
	}

	/**
	 * This method initialises this
	 * 
	 */
	private void initialize() {
		
		setSize(new Point(300,200));
		addMouseWheelListener(new MouseWheelListener(){
			public void mouseScrolled(MouseEvent event)
			{
				selected_slider -= event.count;
				if(selected_slider > slider_max)
				{
					selected_slider =slider_max; 
				}
				else if( selected_slider < 0 )
				{
					selected_slider = 0; 
				}
				slider_.setSelection(selected_slider);
				setSelection();
			}
		});
		
		Rectangle bounds = this.getClientArea();
		table_ = new Table(this, table_style);
		table_.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				tableClicked(); 
			}
		});
		table_.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				keyHandler(e);
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		slider_ = new Slider(this, SWT.VERTICAL);
		slider_.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setSelection();
			}
		}); 
		table_.setSize(bounds.width - 24, bounds.height );
		slider_.setSize(24, bounds.height);
		children_ = new Control[2];
		children_[0]=table_;
		children_[1]=slider_;
		
		this.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				resize(); 
			}
		});
	
		
			
	}
	
	private void keyHandler(KeyEvent evt)
	{
		boolean updated = false; 
		int last = Count ; 
		int first = 0;
		switch(evt.keyCode )
		{
		case SWT.ARROW_DOWN:
		{
			
			if(selected_item < last)
			{
				evt.doit = false;
				selected_item++;
				if(selected_table == number_of_items_on_table -1 )
				{
					selected_slider++; 
					updated = true; 
				}
				else
				{
					selected_table++;
					table_.setSelection(selected_table);
				}
				
			}
			break;
		}
		case SWT.ARROW_UP:
		{
			
			if(selected_item > first)
			{
				evt.doit = false;
				selected_item--;
				if(selected_table == 0)
				{
					selected_slider--; 
					updated = true; 
				}
				else
				{
					selected_table--;
					table_.setSelection(selected_table);
				}
			}
			break;
		}
		case SWT.PAGE_DOWN:
		{
			evt.doit = false;
			if(selected_item < last)
			{
				selected_item += number_of_items_on_table; 
				if(selected_item > last)
				{
					selected_table = number_of_items_on_table - 1;
					selected_item = last;
					selected_slider= slider_max; 
					table_.setSelection(selected_table);
				}
				else
				{
					selected_slider += number_of_items_on_table;
				}
				slider_.setSelection(selected_slider);
				updated = true; 
			}
			break;
		}
		case SWT.PAGE_UP:
		{
			evt.doit = false;
			if(selected_item > first)
			{
				selected_item -= number_of_items_on_table-1; 
				if(selected_item <= first )
				{
					selected_item = first;
					selected_slider = first; 
					selected_table = first; 
					table_.setSelection(selected_table);
				}
				else
				{
					selected_slider -= number_of_items_on_table;
				}
				slider_.setSelection(selected_slider);
				updated = true; 
			}
			break;
		}

		case SWT.END:
		{
			evt.doit = false;
			selected_item = last; 
			selected_table = number_of_items_on_table-1; 
			table_.setSelection(selected_table);
			if( selected_slider != slider_max )
			{
				selected_slider= Count - number_of_items_on_table; 
				slider_.setSelection(selected_slider);
				updated = true;
			}
			break;
		}
		case SWT.HOME:
		{
			evt.doit = false;
			selected_item = first; 
			selected_table = first; 
			table_.setSelection(selected_table);
			if( selected_slider != first )
			{
				selected_slider= first; 
				slider_.setSelection(selected_slider);
				updated = true;
			}
			break;
		}
		};
		if( updated )
		{
			for(int i = 0; i < children_table_items.length; i++)
			{
				checkData( children_table_items[i]);
			}
		}
	}

	boolean checkData(TableItem item) {
		
		int index =table_.indexOf(item); 
		if( index != -1)
		{
			Event evt = new Event();
			evt.item = item;
			evt.index = index + this.selected_slider;
			evt.doit = true;
			this.notifyListeners( SWT.SetData, evt);
			if( item.isDisposed()) return false;
		}
		return true; 
	}
	/*
	 * 
	 */
	private void tableClicked() {
		selected_table = table_.getSelectionIndices()[0];
		selected_item = selected_table + selected_slider; 
		Selected = new TableItem[1];
		Selected[0] = table_.getSelection()[0];
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#getChildren()
	 */
	public Control[] getChildren()
	{
		return children_;	
	}

	/*
	 * 
	 */
	public TableItem[] getSelection()
	{
		return Selected; 
	}
	
	public void setLinesVisible( boolean b)
	{
		table_.setLinesVisible(b);
	}

	public void addSelectionListener( SelectionAdapter sa)
	{
		table_.addSelectionListener(sa);
	}
	
	
	private void setSelection()
	{
		if(( selected_item > selected_slider ) && (selected_item < (selected_slider+number_of_items_on_table )))
		{
			selected_table = selected_item - selected_slider; 
			table_.setSelection(selected_table);
			
		}
		else 
		{
			table_.deselect(selected_table);
		}
		
		for(int i= 0; i < number_of_items_on_table ; i++)
		{
			checkData(children_table_items[i]);
		}
	}
	
	
	
	/*
	 * 
	 */
	public void setItemCount( int items )
	{
		items = Math.max (0, items);
		if(items==Count)return;
		Count = items; 
		resize();
	
	}

	public void setHeaderVisible(boolean b) {
		table_.setHeaderVisible(b);
	}
	/*
	 * 
	 */
	public void resize()
	{
		Rectangle bounds = this.getClientArea();
		int sl_width = slider_.getBounds().width;
		int tab_width = bounds.width - sl_width;
		int tab_height = bounds.height - 10;
		int item_height = table_.getItemHeight();
		number_of_items_on_table = tab_height/item_height;
		if( Count == 0)
			number_of_items_on_table = 0;
		if( number_of_items_on_table > 0 )
		{
			if( table_.getItemCount() != number_of_items_on_table)
			{
				int delta = table_.getItemCount() - number_of_items_on_table;
				if(delta != 0)
				{
					table_.removeAll();
					if(children_table_items != null)
					{
						for( int i = children_table_items.length-1 ; i > 0 ; i-- )
						{
							if( children_table_items[i] != null )
							{
								children_table_items[i].dispose();
							}
							children_table_items[i] = null;
						}
					}
					children_table_items = new TableItem[number_of_items_on_table];
					for( int i = 0 ; i < children_table_items.length; i++ )
					{
						children_table_items[i]= new TableItem(table_, i);
					}
				}
			}
			if( Count < number_of_items_on_table )
			{
				table_.setBounds(bounds);
				table_.setSize(bounds.width, bounds.height+6);
				slider_.setVisible(false);
			}
			else
			{
				table_.setBounds(0, 0, tab_width, bounds.height);
				table_.setSize(tab_width,bounds.height+6);
				slider_.setBounds(tab_width, 0, sl_width, bounds.height);
				slider_.setSize(sl_width, bounds.height);
				slider_.setVisible(true);
				slider_max =  Count - number_of_items_on_table;
				slider_.setMaximum( slider_max);
				slider_.setMinimum(0);
			}	
		}
		else
		{
			table_.setBounds(bounds);
			table_.setSize(bounds.width, bounds.height+6);
			slider_.setVisible(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#setFocus()
	 */
	public boolean setFocus()
	{
		boolean ret_val = this.isVisible();
		if( ret_val )
		{
			for(int i =0 ;i < children_.length; i++)
			{
				children_[i].setFocus();
			}
		}
		return ret_val;
	}

	

	public void createColumnHeaders(ColumnData columnData[]) {
		for (int i = 0; i < columnData.length; i++) {
            TableColumn column = new TableColumn(this.table_, columnData[i].alignment, i);
            column.setText(columnData[i].header);
            column.setWidth(columnData[i].width);
        }
		
	}
    

	public int getTopIndex() {
		
		return this.selected_slider;
	}
	public void setTopIndex( int i){
		slider_.setSelection(i);
	}
	
	public int indexOf(TableItem ti)
	{
		
		return table_.indexOf(ti) +  getTopIndex();
	}
	
	public int removeAll()
	{
		slider_.setMaximum(0);
		table_.removeAll();
		return 0;
	}
	
	public void setSelection(int i)
	{
		if( this.children_table_items != null)
		{
			i = Math.min(i, this.Count);
			i = Math.max(i, 0);
			slider_.setSelection(i);
			setSelection();
		}
	}

	public void createColumnHeaders(ColumnData[] columnData, boolean b) {
		for (int i = 0; i < columnData.length; i++) {
            final TableColumn column = new TableColumn(this.table_, columnData[i].alignment, i);
            column.setText(columnData[i].header);
            column.setWidth(columnData[i].width);
            // TODO: Investigate why the column resizing doesn't work by default
            // Anything to do with SWT_VIRTUAL?
            column.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
				}
				public void widgetSelected(SelectionEvent e) {
					column.pack();
				}
            });
        }
		
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
