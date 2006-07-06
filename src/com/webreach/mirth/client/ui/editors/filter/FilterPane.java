/** FilterPane.java
 *
 *  @author franciscos 
 *  Created on May 26, 2006, 5:08 PM
 */


package com.webreach.mirth.client.ui.editors.filter;

import com.webreach.mirth.client.ui.Mirth;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.CenterCellRenderer;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Rule;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.editors.*;



public class FilterPane extends MirthEditorPane {	
	
	/** CONSTRUCTOR
	 */
	public FilterPane() {
		prevSelRow = -1;
		modified = false;
		initComponents();
	}
	
	/** load( Filter f )
     */
    public void load( Filter f ) {
    	filter = f;
    	
    	// we need to clear all the old data before we load the new
    	makeFilterTable();
    	
    	// add any existing steps to the model
        List<Rule> list = filter.getRules();
        ListIterator<Rule> li = list.listIterator();
        while ( li.hasNext() ) {
        	Rule s = li.next();
        	int row = s.getSequenceNumber();
        	setRowData( s, row );
        }
        
    	int rowCount = filterTableModel.getRowCount();
    	// select the first row if there is one
		if ( rowCount > 0 ) {
			filterTable.setRowSelectionInterval( 0, 0 );
			prevSelRow = 0;
		} else {
			rulePanel.showCard( BLANK_TYPE );
			jsPanel.setData( null );
		}			
    	
    	parent.setCurrentContentPage( this );
    	parent.setCurrentTaskPaneContainer( filterTaskPaneContainer );
    	
    	updateRuleNumbers();
    	updateTaskPane();
    	modified = false;
    }
	
	/** This method is called from within the constructor to
	 *  initialize the form.
	 */
	public void initComponents() {
		
		// the available panels (cards)
		rulePanel = new CardPanel();
		blankPanel = new BlankPanel();
		jsPanel = new JavaScriptPanel( this, "some more notes..." );
		// 		establish the cards to use in the Filter
		rulePanel.addCard( blankPanel, BLANK_TYPE );
		rulePanel.addCard( jsPanel, JAVASCRIPT_TYPE );
		
		filterTablePane = new JScrollPane();
		
		// make and place the task pane in the parent Frame
		filterTaskPaneContainer = new JXTaskPaneContainer();
		
		viewTasks = new JXTaskPane();
		viewTasks.setTitle( "Mirth Views" );
		viewTasks.setFocusable( false );
		
		viewTasks.add(initActionCallback( "accept",
				ActionFactory.createBoundAction( "accept", "Back to Channels", "B" ), 
				new ImageIcon( Frame.class.getResource( "images/resultset_previous.png" )) ));
		parent.setNonFocusable( viewTasks );
		filterTaskPaneContainer.add( viewTasks );
		
		filterTasks = new JXTaskPane();
		filterTasks.setTitle( "Filter Tasks" );
		filterTasks.setFocusable( false );		
		// add new rule task
		filterTasks.add( initActionCallback( "addNewRule",
				ActionFactory.createBoundAction( "addNewRule", "Add New Rule", "N" ),
				new ImageIcon( Frame.class.getResource( "images/add.png" )) ));		
		// delete rule task
		filterTasks.add( initActionCallback( "deleteRule",
				ActionFactory.createBoundAction( "deleteRule", "Delete Rule", "X" ),
				new ImageIcon( Frame.class.getResource( "images/delete.png" )) ));		
		// move rule up task
		filterTasks.add( initActionCallback( "moveRuleUp",
				ActionFactory.createBoundAction( "moveRuleUp", "Move Rule Up", "U" ),
				new ImageIcon( Frame.class.getResource( "images/arrow_up.png" )) ));		
		// move rule down task
		filterTasks.add( initActionCallback( "moveRuleDown",
				ActionFactory.createBoundAction( "moveRuleDown", "Move Rule Down", "D" ),
				new ImageIcon( Frame.class.getResource( "images/arrow_down.png" )) ));		
		// add the tasks to the taskpane, and the taskpane to the mirth client
		parent.setNonFocusable( filterTasks );
		filterTaskPaneContainer.add( filterTasks );
		
		makeFilterTable();
		
		// BGN LAYOUT
		filterTablePane.setBorder( BorderFactory.createEmptyBorder() );
		rulePanel.setBorder( BorderFactory.createEmptyBorder() );
		vSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT,
				filterTablePane, rulePanel );
		vSplitPane.setContinuousLayout( true );
		vSplitPane.setDividerLocation( 200 );
		this.setLayout( new BorderLayout() );
		this.add( vSplitPane, BorderLayout.CENTER );
		this.setBorder( BorderFactory.createEmptyBorder() );
		// END LAYOUT
		
	}  // END initComponents()
	
	public void makeFilterTable() {
		filterTable = new JXTable();
		
		filterTable.setModel( new DefaultTableModel( 
				new String [] { "#", "Operator", "Script" }, 0 ) {
					boolean[] canEdit = new boolean [] { false, true, false };
					
					public boolean isCellEditable( int row, int col ) {
						if ( row == 0 && col == RULE_OP_COL )
							return false;				
						return canEdit[col];
				}
			});
		
		filterTableModel = (DefaultTableModel)filterTable.getModel();
		
		// Set the combobox editor on the operator column, and add action listener
		MyComboBoxEditor comboBox = new MyComboBoxEditor( comboBoxValues );
		((JXComboBox)comboBox.getComponent()).addItemListener( 
				new ItemListener() {
					public void itemStateChanged( ItemEvent evt ) {
						updateOperations();
					}
				});	
		
		filterTable.setSelectionMode( 0 );		// only select one row at a time        
		
		filterTable.getColumnExt( RULE_NUMBER_COL ).setMaxWidth( UIConstants.MAX_WIDTH );
		filterTable.getColumnExt( RULE_OP_COL ).setMaxWidth( UIConstants.MAX_WIDTH );
		
		filterTable.getColumnExt( RULE_NUMBER_COL ).setPreferredWidth( 30 );
		filterTable.getColumnExt( RULE_OP_COL ).setPreferredWidth( 60 );
		
		filterTable.getColumnExt( RULE_NUMBER_COL ).setCellRenderer( new CenterCellRenderer() );
		filterTable.getColumnExt( RULE_OP_COL ).setCellEditor( comboBox );
		
		filterTable.getColumnExt( RULE_NUMBER_COL ).setHeaderRenderer( PlatformUI.CENTER_COLUMN_HEADER_RENDERER );
		filterTable.getColumnExt( RULE_OP_COL ).setHeaderRenderer( PlatformUI.CENTER_COLUMN_HEADER_RENDERER );
		
		filterTable.setRowHeight( UIConstants.ROW_HEIGHT );
		filterTable.packTable( UIConstants.COL_MARGIN );
		filterTable.setSortable( false );
		filterTable.setOpaque( true );
		filterTable.setRowSelectionAllowed( true );
		
        if(Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            filterTable.setHighlighters( highlighter );
        }
                
		filterTable.setBorder( BorderFactory.createEmptyBorder() );
		filterTablePane.setBorder( BorderFactory.createEmptyBorder() );
		
		filterTablePane.setViewportView( filterTable );
		
		filterTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged( ListSelectionEvent evt ) {
						if ( !updating && !evt.getValueIsAdjusting() ) 
							FilterListSelected(evt);
					}
				});
	}    
	
	// for the task pane
	public BoundAction initActionCallback( 
			String callbackMethod, BoundAction boundAction, ImageIcon icon ) {
		
		if(icon != null) boundAction.putValue(Action.SMALL_ICON, icon);
		boundAction.registerCallback( this, callbackMethod );
		return boundAction;
	}
	
	// called whenever a table row is (re)selected
	private void FilterListSelected( ListSelectionEvent evt ) {
		updating = true;
		
		int row = filterTable.getSelectedRow();
		int last = evt.getLastIndex();

		saveData( prevSelRow );
		
		if( isValid( row ) )
			loadData( row );
		else if ( isValid ( last ) ) {
			loadData( last );
			row = last;
		}
		
		rulePanel.showCard( JAVASCRIPT_TYPE );
    	filterTable.setRowSelectionInterval( row, row );
    	prevSelRow = row;
		updateTaskPane();
		
		updating = false;
	}
	
	// returns true if the row is a valid index in the existing model
	private boolean isValid( int row ) {
		return ( row >= 0 && row < filterTableModel.getRowCount() );
	}
	
	// sets the data from the previously used panel into the
	// previously selected Rule object
	private void saveData( int row ) {
		if ( filterTable.isEditing() )
    		filterTable.getCellEditor( filterTable.getEditingRow(), 
    				filterTable.getEditingColumn() ).stopCellEditing();
    	
		updating = true;
		
		if ( isValid( row )) {
			Map<Object,Object> m = jsPanel.getData();
			
			filterTableModel.setValueAt( 
					m.get("Script"), row, RULE_SCRIPT_COL );
		}
		
		updating = false;
	}
	
	// loads the data object from the currently selected row
	// into the correct panel
    private void loadData( int row ) {
    	if ( isValid( row ) ) {
    		Map<Object,Object> m = new HashMap<Object,Object>();
    		m.put("Script", filterTableModel.getValueAt( row, RULE_SCRIPT_COL ));
    		jsPanel.setData( m );
    	}
    }
    
    // display a rule in the table
	private void setRowData( Rule rule, int row ) {
		Object[] tableData = new Object[NUMBER_OF_COLUMNS];
		
		tableData[RULE_NUMBER_COL] = rule.getSequenceNumber();
		tableData[RULE_OP_COL] = rule.getOperator();
		tableData[RULE_SCRIPT_COL] = rule.getScript();

		updating = true;
		filterTableModel.addRow( tableData );
		filterTable.setRowSelectionInterval( row, row );
		updating = false;
	}
	
	/** void addNewRule()
	 *  add a new rule to the end of the list
	 */
	public void addNewRule() {
		modified = true;
		int rowCount = filterTable.getRowCount();
		Rule rule = new Rule();
		
		saveData( filterTable.getSelectedRow() );
		
		rule.setSequenceNumber( rowCount );
		rule.setScript( "" );
		if ( rowCount == 0 )
			rule.setOperator( Rule.Operator.NONE );	// NONE operator by default on row 0
		else
			rule.setOperator( Rule.Operator.AND );	// AND operator by default elsewhere

		setRowData( rule, rowCount );
		prevSelRow = rowCount;
		updateRuleNumbers();
	}
	
	/** void deleteRule(MouseEvent evt)
	 *  delete all selected rows
	 */
	public void deleteRule() {
		modified = true;
		if ( filterTable.isEditing() )
    		filterTable.getCellEditor( filterTable.getEditingRow(), 
    				filterTable.getEditingColumn() ).stopCellEditing();
		
		updating = true; 
		
		int row = filterTable.getSelectedRow();
		if ( isValid( row ) ) {
			filterTableModel.removeRow( row );
			jsPanel.setData( null );
		}
		
		updating = false;
		
		if ( isValid( row ) )
			filterTable.setRowSelectionInterval( row, row );
		else if ( isValid( row - 1 ) )
			filterTable.setRowSelectionInterval( row - 1, row - 1 );
		else {
			rulePanel.showCard( BLANK_TYPE );
			jsPanel.setData( null );
		}
		
		updateRuleNumbers();
	}
	
	/** void moveRule( int i )
	 *  move the selected row i places
	 */
	public void moveRuleUp() { moveRule( -1 ); }
	public void moveRuleDown() { moveRule( 1 ); }
	public void moveRule( int i ) {
		modified = true;		
		int selRow = filterTable.getSelectedRow();
		int moveTo = selRow + i;
		
		// we can't move past the first or last row
		if ( moveTo >= 0 && moveTo < filterTable.getRowCount() ) {
			saveData( selRow );
			loadData( moveTo );
			filterTableModel.moveRow( selRow, selRow, moveTo );
			filterTable.setRowSelectionInterval( moveTo, moveTo );
		}
		
		updateRuleNumbers();
	}
	
	/** void accept(MouseEvent evt)
	 *  returns a vector of vectors to the caller of this.
	 */
	public void accept() {
		saveData( filterTable.getSelectedRow() );
		
		List<Rule> list = new ArrayList<Rule>();
		for ( int i = 0;  i < filterTable.getRowCount();  i++ ) {
			Rule rule = new Rule();
			rule.setSequenceNumber( Integer.parseInt(
					filterTable.getValueAt( i, RULE_NUMBER_COL ).toString() ));
			
			if ( i == 0 )
				rule.setOperator( Rule.Operator.NONE );
			else
				rule.setOperator( Rule.Operator.valueOf(
						filterTableModel.getValueAt( i, RULE_OP_COL ).toString() )); 
			
			rule.setScript( (String)filterTableModel.getValueAt( i, RULE_SCRIPT_COL ));
			
			list.add( rule );
		}
		
		filter.setRules( list );
		
		// reset the task pane and content to channel edit page
		parent.channelEditPage.setSourceVariableList();
		parent.channelEditPage.setDestinationVariableList();
		parent.setCurrentContentPage( parent.channelEditPage );
		parent.setCurrentTaskPaneContainer(parent.taskPaneContainer);
		parent.setPanelName("Edit Channel :: " +  parent.channelEditPage.currentChannel.getName());
		if ( modified ) parent.enableSave();
	}
	
	/** void updateRuleNumbers()
	 *  traverses the table and updates all data numbers, both in the model
	 *  and the view, after any change to the table
	 */
	private void updateRuleNumbers() {    
		updating = true;
		
		int rowCount = filterTableModel.getRowCount();
		int selRow = filterTable.getSelectedRow();
		
		for ( int i = 0;  i < rowCount;  i++ )
    		filterTableModel.setValueAt( i, i, RULE_NUMBER_COL );	

    	updateOperations();
    	if ( isValid( selRow ) ) {
        	filterTable.setRowSelectionInterval( selRow, selRow );
        	loadData( selRow );
        	rulePanel.showCard( JAVASCRIPT_TYPE );
        } else if ( rowCount > 0 ) {
        	filterTable.setRowSelectionInterval( 0, 0 );
        	loadData( 0 );        
        	rulePanel.showCard( JAVASCRIPT_TYPE );
        }
        
    	updateTaskPane();
        updating = false;
    }
	
	/** updateTaskPane()
     *  configure the task pane so that it shows only relevant tasks
     */
    private void updateTaskPane() {
    	int rowCount = filterTableModel.getRowCount();
    	if ( rowCount <= 0 )
        	parent.setVisibleTasks( filterTasks, 1, -1, false );
        else if ( rowCount == 1 ) {
        	parent.setVisibleTasks( filterTasks, 0, -1, true );
        	parent.setVisibleTasks( filterTasks, 2, -1, false );
        } else {
        	parent.setVisibleTasks( filterTasks, 0, -1, true );
        	
        	int selRow = filterTable.getSelectedRow();
        	if ( selRow == 0 ) // hide move up
        		parent.setVisibleTasks( filterTasks, 2, 2, false );
        	else if ( selRow == rowCount - 1 ) // hide move down
        		parent.setVisibleTasks( filterTasks, 3, 3, false );
        }
    }
    
    /** updateOperations()
     *  goes through all existing rules, enforcing rule 0 to be 
     *  a Rule.Operator.NONE, and any other NONEs to ANDs.
     */
    private void updateOperations() {
    	for ( int i = 0;  i < filterTableModel.getRowCount(); i++ ) {
    		if ( i == 0 )
    			filterTableModel.setValueAt( "", i, RULE_OP_COL );
    		else if ( filterTableModel.getValueAt( i, RULE_OP_COL ).toString().equals( "" ) )
    			filterTableModel.setValueAt( Rule.Operator.AND.toString(), i, RULE_OP_COL );
    	}
    }
	
//............................................................................\\
	
	// used to load this pane
	private Filter filter;
	
	// fields
	private JXTable filterTable;
	private DefaultTableModel filterTableModel;
	private JScrollPane filterTablePane;
	private JSplitPane vSplitPane;
	private boolean updating;				// allow the selection listener to breathe
	JXTaskPaneContainer filterTaskPaneContainer = new JXTaskPaneContainer();
	JXTaskPane viewTasks = new JXTaskPane();
	JXTaskPane filterTasks = new JXTaskPane();
	
	
	// this little sucker is used to track the last row that had
	// focus after a new row is selected
	private int prevSelRow;	// no row by default
	
	// panels using CardLayout
	protected CardPanel rulePanel;		// the card holder
	protected BlankPanel blankPanel;	// the cards
	protected JavaScriptPanel jsPanel;  //    \/
	
	// filter constants
	public static final int RULE_NUMBER_COL  = 0;
	public static final int RULE_OP_COL  = 1;
	public static final int RULE_SCRIPT_COL  = 2;
	public static final int NUMBER_OF_COLUMNS = 3;
	public static final String BLANK_TYPE = "";
	public static final String JAVASCRIPT_TYPE = "JavaScript";
	private String[] comboBoxValues = new String[] { 
			Rule.Operator.AND.toString(), Rule.Operator.OR.toString() };
	
}
