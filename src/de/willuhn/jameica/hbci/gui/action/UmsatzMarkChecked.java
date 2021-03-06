/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.rmi.Flaggable;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Action, um einen Umsatz als geprueft zu markieren.
 */
public class UmsatzMarkChecked extends FlaggableChange
{
  private Boolean assign = null;

  /**
   * ct.
   * @param flags die zu setzenden Flags.
   * @param add true, wenn Flags hinzugefuegt werden sollen. Andernfalls werden sie entfernt.
   */
  public UmsatzMarkChecked(int flags, boolean add)
  {
    super(flags,add);
  }
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    this.assign = null;
    super.handleAction(context);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.action.FlaggableChange#postProcess(de.willuhn.jameica.hbci.rmi.Flaggable)
   */
  @Override
  protected void postProcess(Flaggable o) throws Exception
  {
    // Nur, wenn das Flag gesetzt wird
    if (!this.getAdd())
      return;
    
    if (!(o instanceof Umsatz))
      return;

    Umsatz u = (Umsatz) o;
    UmsatzTyp ut = u.getUmsatzTyp();
    
    // Wir haben gar keine Kategorie - dann eruebrigt sich die Frage.
    if (ut == null)
      return;

    // Wir checken das nur einmal pro Aufruf
    if (this.assign == null)
    {
      // Ermitteln, ob der User gefragt werden soll, ob er die Kategorien dabei fest zuordnen will
      String s = i18n.tr("Sollen Umsatz-Kategorien, die dynamisch per Suchbegriff zugeordnet wurden,\n" +
                         "hierbei fest mit den Umsätzen verbunden werden? Das ermöglicht eine spätere\n" +
                         "Änderung des Suchbegriffes in der Kategorie, ohne dass hierbei die Zuordnung\n" +
                         "ggf. wieder verloren geht.\n\n" +
                         "Kategorien hierbei fest zuordnen?");
      this.assign = Boolean.valueOf(Application.getCallback().askUser(s));
    }
    
    if (this.assign != null && this.assign.booleanValue())
      u.setUmsatzTyp(ut);
  }

}


