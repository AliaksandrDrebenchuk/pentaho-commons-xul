package org.pentaho.ui.xul.swing.tags;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Expression;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.containers.XulRoot;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.swing.SwingElement;

public class SwingMenuList<T> extends SwingElement implements XulMenuList<T> {

  private JComboBox combobox;

  private XulDomContainer xulDomContainer;

  private static final Log logger = LogFactory.getLog(SwingMenuList.class);

  private boolean loaded = false;

  private String binding;

  public SwingMenuList(Element self, XulComponent parent, XulDomContainer domContainer, String tagName) {
    super("menulist");

    this.xulDomContainer = domContainer;

    children = new ArrayList<XulComponent>();

    combobox = new JComboBox(new DefaultComboBoxModel());
    managedObject = combobox;

  }

  public void layout() {
    SwingMenupopup popup = getPopupElement();
    DefaultComboBoxModel model = (DefaultComboBoxModel) this.combobox.getModel();
    model.removeAllElements();

    for (XulComponent item : popup.getChildNodes()) {
      JMenuItem jmenuItem = (JMenuItem) ((SwingMenuitem) item).getManagedObject();
      model.addElement(jmenuItem.getText());
    }
    loaded = true;
  }

  public SwingMenupopup getPopupElement() {
    for (XulComponent comp : children) {
      if (SwingMenupopup.class.isAssignableFrom(comp.getClass())) {
        return (SwingMenupopup) comp;
      }
    }
    throw new IllegalStateException("menulist is missing a menupopup child element");
  }

  /*
   * Swaps out the managed list.  Effectively replaces the SwingMenupopup child component.
   * (non-Javadoc)
   * @see org.pentaho.ui.xul.components.XulMenuList#replaceAll(java.util.List)
   */
  public void replaceAllItems(Collection<T> tees) {
    setElements(tees);
  }

  public String getSelectedItem() {
    return (String) this.combobox.getModel().getSelectedItem();
  }

  public String setSelectedItem(T t) {
    return (String) this.combobox.getModel().getSelectedItem();
  }

  public void setOncommand(final String command) {
    combobox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {

        /*
         * This actionlistener is fired at parse time when elements are added.
         * We'll ignore that call by checking a variable set to true post parse time
         */
        if (!loaded) {
          return;
        }
        Document doc = getDocument();
        XulRoot window = (XulRoot) doc.getRootElement();
        final XulDomContainer con = window.getXulDomContainer();

        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            try {
              con.invoke(command, new Object[] {});
            } catch (XulException e) {
              logger.error("Error calling oncommand event", e);
            }
          }
        });

      }
    });
  }

  public Collection<T> getElements() {
    return null;
  }

  public String getBinding() {
    return binding;
  }

  public void setBinding(String binding) {
    this.binding = binding;
  }

  private String extractLabel(T t) {
    String attribute = getBinding();
    if (StringUtils.isEmpty(attribute)) {
      return t.toString();
    } else {
      String getter = "get" + (String.valueOf(attribute.charAt(0)).toUpperCase()) + attribute.substring(1);
      try {
        return new Expression(t, getter, null).getValue().toString();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void setElements(Collection<T> tees) {
    XulMenupopup popup = getPopupElement();
    for (XulComponent menuItem : popup.getChildNodes()) {
      popup.removeChild(menuItem);
    }

    for (T t : tees) {
      SwingMenuitem item = new SwingMenuitem(null, popup, this.xulDomContainer, null);

      String attribute = getBinding();
      if (StringUtils.isEmpty(attribute)) {
        item.setLabel(extractLabel(t));
      }

      popup.addChild(item);
    }

    layout();
  }
}