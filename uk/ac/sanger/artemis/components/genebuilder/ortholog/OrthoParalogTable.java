/* OrthoParalogTable.java
 * This file is part of Artemis
 *
 * Copyright (C) 2007  Genome Research Limited
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package uk.ac.sanger.artemis.components.genebuilder.ortholog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import uk.ac.sanger.artemis.Feature;
import uk.ac.sanger.artemis.chado.ArtemisUtils;
import uk.ac.sanger.artemis.io.PartialSequence;
import uk.ac.sanger.artemis.io.Qualifier;
import uk.ac.sanger.artemis.io.QualifierVector;
import uk.ac.sanger.artemis.util.DatabaseDocument;
import uk.ac.sanger.artemis.util.StringVector;

public class OrthoParalogTable extends AbstractMatchTable
{
  private static int NUMBER_COLUMNS = 9;
  private Vector rowData   = new Vector();
  private Vector tableData = new Vector(NUMBER_COLUMNS);
  private JTable table;
  private JButton infoLevelButton = new JButton("Details");
  private JPopupMenu popupMenu = new JPopupMenu();

  //
  // column headings
  final static String CLUSTER_NAME_COL = "Cluster";
  final static String MATCH_NAME_COL = "Match";
  final static String ROW_TYPE_HIDE_COL = "Term";
  final static String ROW_TYPE_COL = "Type";
  final static String ORGANISM_COL = "Organism";
  final static String ORTHO_COL = "Gene";
  final static String LINK_COL = "Link";
  final static String DESCRIPTION_COL = "Description";
  final static String REMOVE_BUTTON_COL = "";
  
  /**
   * Contruct a component for an ortholog or paralog line
   * @param doc
   * @param origQualifier
   * @param feature
   */
  protected OrthoParalogTable(final DatabaseDocument doc,
                          final Qualifier orthologQualifier,
                          final Qualifier paralogQualifier,
                          final Feature feature)
  {
    this.origQualifiers = new QualifierVector();
    
    if(orthologQualifier != null)
      this.origQualifiers.add(orthologQualifier);
    if(paralogQualifier != null)
      this.origQualifiers.add(paralogQualifier);
    
    createPopupMenu(doc, feature);
    
    infoLevelButton.setOpaque(false);
    tableData.setSize(NUMBER_COLUMNS);
    
    tableData.setElementAt(CLUSTER_NAME_COL,0);
    tableData.setElementAt(MATCH_NAME_COL,1);
    tableData.setElementAt(ROW_TYPE_HIDE_COL,2);
    tableData.setElementAt(ROW_TYPE_COL,3);
    tableData.setElementAt(ORGANISM_COL,4);
    tableData.setElementAt(ORTHO_COL,5);
    tableData.setElementAt(LINK_COL,6);
    tableData.setElementAt(DESCRIPTION_COL,7);
    tableData.setElementAt(REMOVE_BUTTON_COL,8);

    
    // add row data
    int columnIndex;
      
    for(int i=0; i<origQualifiers.size(); i++)
    {
      Qualifier origQualifier = (Qualifier) origQualifiers.elementAt(i);
      StringVector values = origQualifier.getValues();

      // sort by their rank value
      Collections.sort(values, new OrthoParalogValueComparator());

      for(int j = 0; j < values.size(); j++)
      {
        StringVector rowStr = StringVector.getStrings((String) values.get(j),
            ";");

        final String orthoparalogs[] = ((String) rowStr.get(0)).split(",");
        String description = "";
        if(rowStr.size() > 1)
        {
          description = ArtemisUtils.getString(rowStr, "description=");
          if(!description.equals(""))
            description = description.substring(12);
        }

        String clusterName = "";
        if(rowStr.size() > 1)
        {
          clusterName = ArtemisUtils.getString(rowStr, "cluster_name=");
          if(!clusterName.equals(""))
            clusterName = clusterName.substring(13);
        }
        
        String matchName = "";
        if(rowStr.size() > 1)
        {
          matchName = ArtemisUtils.getString(rowStr, "match_name=");
          if(!matchName.equals(""))
            matchName = matchName.substring(11);
        }
        
        for(int k = 0; k < orthoparalogs.length; k++)
        {
          Vector thisRowData = new Vector(NUMBER_COLUMNS);
          
          String geneNameAndLink[] = orthoparalogs[k].split("link=");
          String gene[] = geneNameAndLink[0].trim().split(":");
          
          thisRowData.setSize(NUMBER_COLUMNS);
          
          columnIndex = tableData.indexOf(ORGANISM_COL);
          thisRowData.setElementAt(gene[0], columnIndex);
          
          columnIndex = tableData.indexOf(ORTHO_COL);
          thisRowData.setElementAt(geneNameAndLink[0].trim(), columnIndex);
          
          columnIndex = tableData.indexOf(LINK_COL);
          thisRowData.setElementAt(geneNameAndLink[1].trim(), columnIndex);
          
          columnIndex = tableData.indexOf(CLUSTER_NAME_COL);
          thisRowData.setElementAt(clusterName, columnIndex);

          columnIndex = tableData.indexOf(MATCH_NAME_COL);
          thisRowData.setElementAt(matchName, columnIndex);
          
          columnIndex = tableData.indexOf(DESCRIPTION_COL);
          thisRowData.setElementAt(description, columnIndex);
          
          columnIndex = tableData.indexOf(ROW_TYPE_HIDE_COL);
          thisRowData.setElementAt(origQualifier.getName(), columnIndex);
          
          columnIndex = tableData.indexOf(ROW_TYPE_COL);
          
          final String symbol;
          if(origQualifier.getName().equals(MatchPanel.ORTHOLOG))
            symbol = "O";
          else
            symbol = "P";
          
          thisRowData.setElementAt(symbol, columnIndex);
          rowData.add(thisRowData);
        }
      }
    }
    
    table = new JTable(rowData, tableData);
    setTable(table);
    
    // set hand cursor
    table.addMouseMotionListener( new MouseMotionAdapter() 
    {
      private Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      public void mouseMoved(MouseEvent e) 
      {
        int col = table.columnAtPoint(e.getPoint());
        
        String colName = table.getColumnName(col);
     
        if(colName.equals(ORTHO_COL) || colName.equals(REMOVE_BUTTON_COL)) 
          table.setCursor(handCursor);
        else 
          table.setCursor(Cursor.getDefaultCursor());  
      }
    });
    
    table.addMouseListener(new MouseAdapter() 
    {
      public void mousePressed(MouseEvent e) 
      {
        showPopup(e);
      }

      public void mouseReleased(MouseEvent e) 
      {
        showPopup(e);
      }

      private void showPopup(MouseEvent e)
      {
        if(e.isPopupTrigger()) 
          popupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    });
    
    packColumn(table, getColumnIndex(DESCRIPTION_COL), 4);
    packColumn(table, getColumnIndex(ROW_TYPE_COL), 4);
    packColumn(table, getColumnIndex(CLUSTER_NAME_COL), 4);
    packColumn(table, getColumnIndex(ORTHO_COL), 4);
    
    final TableColumn[] hideColumns = new TableColumn[2];
    hideColumns[0] = table.getColumn(ROW_TYPE_HIDE_COL);
    hideColumns[1] = table.getColumn(MATCH_NAME_COL);
    
    for(int i=0; i<hideColumns.length; i++)
    {
      hideColumns[i].setMinWidth(0);
      hideColumns[i].setMaxWidth(0);
    }

    table.setColumnSelectionAllowed(false);
    table.setRowSelectionAllowed(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    table.setDragEnabled(true);
    table.setTransferHandler(new TableTransferHandler());
    
    TableModel tableModel = table.getModel();
    // remove button column
    TableColumn col = table.getColumn(REMOVE_BUTTON_COL);
    col.setMinWidth(35);
    col.setMaxWidth(40);
    col.setPreferredWidth(40);

    final OrthologRenderer renderer = new OrthologRenderer();

    for(columnIndex = 0; columnIndex <tableModel.getColumnCount();
        columnIndex++) 
    {
      col = table.getColumnModel().getColumn(columnIndex);
      col.setCellRenderer(renderer);
      col.setCellEditor(new CellEditing(new JTextField()));
    }
    
    // remove JButton column
    col = table.getColumn(REMOVE_BUTTON_COL);
    col.setCellEditor(new ButtonEditor(new JCheckBox(),
        (DefaultTableModel)table.getModel()));
    
    // orthologue link
    col = table.getColumn(ORTHO_COL);
    col.setCellEditor(new LinkEditor(new JCheckBox(),
        (DefaultTableModel)table.getModel(), doc));
  }
  
  /**
   * Create the popup menu for the table
   *
   */
  private void createPopupMenu(final DatabaseDocument doc,
                               final Feature feature)
  {
    JMenuItem showSequenceMenu = new JMenuItem("Show selected sequences");
    popupMenu.add(showSequenceMenu);
    showSequenceMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        final int[] rows = table.getSelectedRows();
        final int column = getColumnIndex(ORTHO_COL);
        final Vector seqs = new Vector();
        
        
        final String bases = feature.getTranslationBases();
        //final String bases = feature.getBases();
        final String sysName = feature.getSystematicName();
        seqs.add(new org.emboss.jemboss.editor.Sequence(sysName, bases));
        
        for(int row=0; row<rows.length; row++)
        {
          String ortho = (String)table.getValueAt(row, column);
          final String reference[] = ortho.split(":");
          DatabaseDocument newdoc = new DatabaseDocument(doc, 
              reference[0], reference[1], true);
          
          try
          {
            PartialSequence sequence = newdoc.getChadoSequence(reference[1]);

            seqs.add(new org.emboss.jemboss.editor.Sequence(ortho, new String(
                sequence.getSequence())));
          }
          catch(NullPointerException npe)
          {
            JOptionPane.showMessageDialog(null, 
                "Cannot get the sequence for "+ortho,
                "Warning", JOptionPane.WARNING_MESSAGE);
          }
        }
        
        org.emboss.jemboss.editor.AlignJFrame ajFrame =
              new org.emboss.jemboss.editor.AlignJFrame(seqs);
        ajFrame.setVisible(true);
      }
    });
  }
  

  /**
   * Called by AbstractMatchTable.updateQualifier()
   */
  protected String updateQualifierString(final int row)
  {
    StringBuffer orthologStr = new StringBuffer(
        (String)getTable().getValueAt(row, getColumnIndex(ORTHO_COL))+
        " link="+
        (String)getTable().getValueAt(row, getColumnIndex(LINK_COL)) );            // ortholog link
    orthologStr.append(";");
    
    String description = (String)getTable().getValueAt(row, getColumnIndex(DESCRIPTION_COL));
    if(description != null && !description.equals(""))
      orthologStr.append(description+ ";" ); // description
    
    String clusterName = (String)getTable().getValueAt(row, getColumnIndex(CLUSTER_NAME_COL));
    if(clusterName != null && !clusterName.equals(""))
      orthologStr.append("cluster_name="+clusterName+ ";" ); // cluster name
    
    String matchName = (String)getTable().getValueAt(row, getColumnIndex(MATCH_NAME_COL));
    if(matchName != null && !matchName.equals(""))
      orthologStr.append("match_name="+matchName+ ";" ); // match name
    
    orthologStr.append("rank="+row);
    
    return orthologStr.toString();
  }
  
  /**
   * Returns true if the qualifier name matches the row type, e.g.
   * orthologous_to / paralogous_to
   * @param qualifierName
   * @param row
   * @return
   */
  protected boolean isRowOfType(String qualifierName, int row)
  {
    String rowType = (String)getTable().getValueAt(row, getColumnIndex(ROW_TYPE_HIDE_COL));
    
    if(rowType.equals(qualifierName))
      return true;
    return false;
  }

  /**
   * Check whether ortholog/paralog qualifier string exists in a StringVector for that qualifier.
   * If the StringVector contains the hit, description return true.
   * @param qualStr
   * @param qualStringVector
   * @return
   */
  public static boolean containsStringInStringVector(final String qualStr, 
                                                     final StringVector qualStringVector)
  {
    final StringVector orth1 = StringVector.getStrings(qualStr, ";");
    final String clusterName1 = ArtemisUtils.getString(orth1, "cluster");
    //final String rank1 = ArtemisUtils.getString(orth1, "rank");
    String value1 = (String)orth1.get(0);
    int index = value1.indexOf('=');
    if(index > -1)
      value1 = value1.substring(index+1);
    
    for(int i=0; i<qualStringVector.size(); i++)
    {
      String thisStr = (String)qualStringVector.get(i);
      
      StringVector orth2 = StringVector.getStrings(thisStr, ";");
      
      if(orth1.size() != orth2.size())
        continue;
      
      String value2 = (String)orth2.get(0);
      if((index = value2.indexOf('=')) > -1)
        value2 = value2.substring(index+1);
      
      if(!clusterName1.equals("") && !ArtemisUtils.getString(orth2, "cluster_name").equals(""))
        System.out.println(value1+"  ==>  "+value2);
      // ortholog/paralog/cluster
      if(value1.indexOf(value2) < 0 &&
         value2.indexOf(value1) < 0 )
        continue;
      
      // cluster name
      final String clusterName2 = ArtemisUtils.getString(orth2, "cluster_name");
      if(!clusterName1.equals(clusterName2))
        continue;
      
      // rank
      /*
      final String rank2 = ArtemisUtils.getString(orth2, "rank");
      if(!rank1.equals(rank2))
        continue;
      */
      
      // description
      /*
      if( orth1.size() > 1 && orth2.size() > 1 &&
          !((String)orth1.get(1)).equals((String)orth2.get(1)) )
        continue;
      */
      
      return true; 
    }
    return false;
  }
  
  /**
   * Renderer for the Ortholog cells
   */
  private class OrthologRenderer extends DefaultTableCellRenderer
  {  
    /** */
    private static final long serialVersionUID = 1L;
    private int minHeight = -1;
    
    private final JLabel orthologLabel = new JLabel();
    private final JLabel link = new JLabel();
    private final JLabel type = new JLabel();
    private final JLabel symbol = new JLabel();
    private final JLabel organism = new JLabel();
    private final JTextArea descriptionTextArea = new JTextArea();
    private final JLabel clusterName = new JLabel();
    private final JLabel matchName = new JLabel();
    private final JLabel buttRemove = new JLabel("X");
    private Color fgColor = new Color(139,35,35);
    private Color fgLinkColor = Color.BLUE;
    
    public OrthologRenderer() 
    {
      orthologLabel.setForeground(Color.BLUE);
      orthologLabel.setOpaque(true);
      
      symbol.setOpaque(true);
      symbol.setHorizontalAlignment(SwingConstants.CENTER);

      clusterName.setOpaque(true);
      organism.setOpaque(true);
      link.setOpaque(true);
      
      descriptionTextArea.setLineWrap(true);
      descriptionTextArea.setWrapStyleWord(true);

      buttRemove.setOpaque(true);
      buttRemove.setText("X");
      
      Font font = getFont().deriveFont(Font.BOLD);
      buttRemove.setFont(font);
      buttRemove.setToolTipText("REMOVE");
      buttRemove.setHorizontalAlignment(SwingConstants.CENTER);
    }
    

    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        final int row,
        final int column) 
    {
      Component c = null;
      String text = null;
      if(value != null)
        text = (String)value;
      Dimension dim;

      TableColumn tableCol;
      if(column == getColumnIndex(ORTHO_COL))
      {
        String gene[] = text.split(":");
        orthologLabel.setText(gene[1]);
        
        if(isSelected) 
        {
          orthologLabel.setForeground(fgLinkColor);
          orthologLabel.setBackground(table.getSelectionBackground());
        } 
        else
        {
          orthologLabel.setForeground(fgLinkColor);
          orthologLabel.setBackground(UIManager.getColor("Button.background"));
        }
        
        c = orthologLabel;
      }
      else if(column == getColumnIndex(ORGANISM_COL))
      {
        organism.setText(text);
        c = organism;
      }
      else if(column == getColumnIndex(LINK_COL))
      {
        link.setText(text);
        c = link;
      }
      else if(column == getColumnIndex(DESCRIPTION_COL))
      {
        descriptionTextArea.setText(text);

        tableCol = table.getColumnModel().getColumn(column);
        descriptionTextArea.setSize(tableCol.getWidth(), table
            .getRowHeight(row));

        dim = descriptionTextArea.getPreferredSize();
        minHeight = Math.max(minHeight, dim.height);
        c = descriptionTextArea;
      }
      else if(column == getColumnIndex(CLUSTER_NAME_COL))
      {
        clusterName.setText(text);

        tableCol = table.getColumnModel().getColumn(column);
        clusterName.setSize(tableCol.getWidth(), table
            .getRowHeight(row));

        dim = clusterName.getPreferredSize();
        minHeight = Math.max(minHeight, dim.height);
        c = clusterName;
      }
      else if(column == getColumnIndex(MATCH_NAME_COL))
      {
        matchName.setText(text);
        c = matchName;
      }
      else if(column == getColumnIndex(ROW_TYPE_HIDE_COL))
      {
        type.setText(text);
        c = type;
      }
      else if(column == getColumnIndex(ROW_TYPE_COL))
      {
        symbol.setText(text);
        tableCol = table.getColumnModel().getColumn(column);
        symbol.setSize(tableCol.getWidth(), table
            .getRowHeight(row));

        dim = symbol.getPreferredSize();
        minHeight = Math.max(minHeight, dim.height);
        c = symbol;
      }
      else if(column == getColumnIndex(REMOVE_BUTTON_COL))
      {
        if(isSelected) 
        {
          buttRemove.setForeground(fgColor);
          buttRemove.setBackground(table.getSelectionBackground());
        } 
        else
        {
          buttRemove.setForeground(fgColor);
          buttRemove.setBackground(UIManager.getColor("Button.background"));
        }
        c = buttRemove;
      }
      else
      {
        throw new RuntimeException("invalid column! " + column +
                                   " " + text);
      }

      // adjust row height for columns with multiple lines
      if(column < 3)
      {
        if(table.getRowHeight(row) < minHeight)
          table.setRowHeight(row, minHeight);

        minHeight = -1;
      }
      
      // highlight on selection
      if(isSelected) 
        c.setBackground(table.getSelectionBackground()); 
      else
        c.setBackground(Color.white);
      
      return c;
    }
  }

  public class OrthoParalogValueComparator implements Comparator
  {
    public int compare(Object o1, Object o2)
    {
      final String value1 = (String)o1;
      final String value2 = (String)o2;
      
      StringVector values1 = StringVector.getStrings((String)value1, ";");
      String rank1 = ArtemisUtils.getString(values1, "rank");
      if(!rank1.equals(""))
      {
        if(rank1.startsWith("rank=") || rank1.startsWith("rank "))
          rank1 = rank1.substring(5);
      }
      else
        rank1 = "0";
      
      StringVector values2 = StringVector.getStrings((String)value2, ";");
      String rank2 = ArtemisUtils.getString(values2, "rank");
      if(!rank2.equals(""))
      {
        if(rank2.startsWith("rank=") || rank2.startsWith("rank "))
          rank2 = rank2.substring(5);
      }
      else
        rank2 = "0";
      
      
      return (new Integer(rank1)).compareTo(new Integer(rank2));
    }   
  }
}