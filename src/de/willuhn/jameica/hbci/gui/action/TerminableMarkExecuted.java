/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/TerminableMarkExecuted.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/02/18 10:48:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Markiert eine Ueberweisung/Lastschrift als ausgefuehrt.
 */
public class TerminableMarkExecuted implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    if (context == null)
      return;

    Terminable t[] = null;
    if (context instanceof Terminable)
      t = new Terminable[]{(Terminable) context};
    else
      t = (Terminable[]) context;
    
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    d.setTitle(i18n.tr("Sicher?"));
    if (t.length == 1)
      d.setText(i18n.tr("Sind Sie sicher, dass Sie diesen Auftrag als \"ausgeführt\" markieren wollen?\nDies kann nicht rückgängig gemacht werden."));
    else
      d.setText(i18n.tr("Sind Sie sicher, dass Sie diese {0} Aufträge als \"ausgeführt\" markieren wollen?\nDies kann nicht rückgängig gemacht werden.",""+t.length));
    
    try
    {
      Boolean b = (Boolean) d.open();
      if (b == null || !b.booleanValue())
        return;
      
      for (int i=0;i<t.length;++i)
      {
        t[i].setAusgefuehrt(true);
        if (t[i] instanceof HibiscusTransfer)
        {
          HibiscusTransfer tr = (HibiscusTransfer) t[i];
          Konto k = tr.getKonto();
          if (k != null)
            k.addToProtokoll(i18n.tr("Auftrag \"{0}\" [Gegenkonto {1}, BLZ {2}] manuell als \"ausgeführt\" markiert",new String[]{tr.getZweck(),tr.getGegenkontoName(),tr.getGegenkontoBLZ()}),Protokoll.TYP_SUCCESS);
          Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(tr));
        }
        else if (t[i] instanceof SammelTransfer)
        {
          SammelTransfer tr = (SammelTransfer) t[i];
          Konto k = tr.getKonto();
          if (k != null)
            k.addToProtokoll(i18n.tr("Sammel-Auftrag [Bezeichnung: {0}] manuell als \"ausgeführt\" markiert",tr.getBezeichnung()),Protokoll.TYP_SUCCESS);
          Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(tr));
        }
      }
      if (t.length == 1)
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Auftrag als \"ausgeführt\" markiert"),StatusBarMessage.TYPE_SUCCESS));
      else
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Aufträge als \"ausgeführt\" markiert"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (OperationCanceledException oce)
    {
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unabel to mark transfers as executed",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Markieren als \"ausgeführt\""),StatusBarMessage.TYPE_ERROR));
    }
    
  }

}
