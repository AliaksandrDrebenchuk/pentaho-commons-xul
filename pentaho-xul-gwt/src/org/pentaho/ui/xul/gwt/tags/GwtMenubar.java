/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.ui.xul.gwt.tags;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulMenuseparator;
import org.pentaho.ui.xul.containers.XulMenubar;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.gwt.AbstractGwtXulContainer;
import org.pentaho.ui.xul.gwt.GwtXulHandler;
import org.pentaho.ui.xul.gwt.GwtXulParser;
import org.pentaho.ui.xul.gwt.util.FrameCover;
import org.pentaho.ui.xul.stereotype.Bindable;

public class GwtMenubar extends AbstractGwtXulContainer implements XulMenubar {

  private String menubarName;
  private MenuBar menubar;
  private boolean loaded;
  private boolean vertical = true;
  private FrameCover frameCover = null;

  public static void register() {
    GwtXulParser.registerHandler( "menubar", new GwtXulHandler() {
      public Element newInstance() {
        return new GwtMenubar();
      }
    } );
  }

  public GwtMenubar() {
    super( "menubar" );
  }

  public GwtMenubar( String tagName ) {
    super( tagName );
  }

  @Override
  public void init( com.google.gwt.xml.client.Element srcEle, XulDomContainer container ) {
    this.setHorizontal( "vertical".equalsIgnoreCase( srcEle.getAttribute( "layout" ) ) );
    frameCover = new FrameCover();

    menubar = new MenuBar( vertical ) {
      @Override
      public void onBrowserEvent( Event event ) {
        switch ( DOM.eventGetType( event ) ) {
          case Event.ONMOUSEOVER:
          case Event.ONCLICK:
            frameCover.cover();
            break;
        }
        super.onBrowserEvent( event );
      }
    };

    frameCover.addClickHandler( new ClickHandler() {
      @Override
      public void onClick( ClickEvent event ) {
        menubar.closeAllChildren( true );
      }
    } );

    menubar.addCloseHandler( new CloseHandler<PopupPanel>() {
      @Override
      public void onClose( CloseEvent<PopupPanel> event ) {
        if ( !menubar.isVisible() ) {
          frameCover.remove();
        }
      }
    } );

    this.setLabel( srcEle.getAttribute( "label" ) );
    setManagedObject( menubar );

    // init AFTER we set the managed object and we get "id" set for us
    super.init( srcEle, container );

  }

  public void setLabel( String label ) {
    menubar.setTitle( label );
  }

  public String getLabel() {
    return menubar.getTitle();
  }

  public boolean isHorizontal() {
    return vertical;
  }

  public void setHorizontal( boolean horizontal ) {
    this.vertical = horizontal;
  }

  public String getMenubarName() {
    return menubarName;
  }

  public void setMenubarName( String name ) {
    this.menubarName = name;
  }

  @Bindable
  public void setVisible( boolean visible ) {
    this.visible = visible;
    menubar.getElement().getStyle().setProperty( "display", ( this.visible ) ? "" : "none" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    if ( getParent() instanceof GwtMenubar ) {
      ( (MenuBar) getParent().getManagedObject() ).clearItems();
      ( (GwtMenubar) getParent() ).layout();
    }
  }

  @Override
  public void layout() {
    menubar.clearItems();
    for ( XulComponent c : this.getChildNodes() ) {
      add( c );
    }
    if ( !loaded ) {
      loaded = true;
    }
  }

  private void add( XulComponent c ) {
    if ( c instanceof XulMenuitem ) {
      MenuItem item = (MenuItem) c.getManagedObject();
      menubar.addItem( item );
    } else if ( c instanceof XulMenuseparator ) {
      menubar.addSeparator();
    } else if ( c instanceof XulMenubar ) {
      if ( c.isVisible() ) {
        MenuBar bar = (MenuBar) c.getManagedObject();
        MenuItem submenu = new MenuItem( bar.getTitle(), bar );
        submenu.getElement().setId( bar.getElement().getId() );
        menubar.addItem( submenu );
      }
    }
  }

  @Override
  public void addChild( Element element ) {
    super.addChild( element );
    if ( loaded == true ) {
      menubar.clearItems();
      this.layout();
    }
  }

  @Override
  public void addChildAt( Element element, int idx ) {
    super.addChildAt( element, idx );
    if ( loaded == true ) {
      menubar.clearItems();
      this.layout();
    }
  }

  @Override
  public void removeChild( Element element ) {
    super.removeChild( element );
    XulComponent child = (XulComponent) element;
    if ( child instanceof XulMenuitem ) {
      menubar.removeItem( (MenuItem) child.getManagedObject() );
    }

  }

}
