/*
 *
 * This file is part of the Documentum Elasticus project.
 * Copyright (c) 2010-2011 Limited Liability Company «MEZHGALAKTICHESKIJ TORGOVYJ ALIANS»
 * Author: Alexey Aksenov
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Global License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED
 * BY Limited Liability Company «MEZHGALAKTICHESKIJ TORGOVYJ ALIANS»,
 * Limited Liability Company «MEZHGALAKTICHESKIJ TORGOVYJ ALIANS» DISCLAIMS
 * THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Global License for more details.
 * You should have received a copy of the GNU Affero General Global License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://www.gnu.org/licenses/agpl.html
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Global License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Global License,
 * you must retain the producer line in every report, form or document
 * that is created or manipulated using Documentum Elasticus.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the Documentum Elasticus software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers,
 * serving files in a web or/and network application,
 * shipping Documentum Elasticus with a closed source product.
 *
 * For more information, please contact Documentum Elasticus Team at this
 * address: ezh@ezh.msk.ru
 *
 */

package org.digimead.documentumelasticus.storage

import com.sun.star.awt.XDialog
import com.sun.star.awt.XDialogEventHandler
import com.sun.star.deployment.XPackageInformationProvider
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.Extension
import org.digimead.documentumelasticus.XUserUNO
import org.digimead.documentumelasticus.component.XBase
import org.digimead.documentumelasticus.component.XBaseInfo
import org.digimead.documentumelasticus.helper._
import org.slf4j.LoggerFactory
import java.io.{File => JFile}

class Office(val ctx: XComponentContext) extends XStorage {
  protected val logger = LoggerFactory.getLogger(this.getClass)
  var componentSingleton = Office.componentSingleton
  val componentTitle = Office.componentTitle
  val componentDescription = Office.componentDescription
  val componentURL = Office.componentURL
  val componentName = Office.componentName
  val componentServices = Office.componentServices
  val componentDisabled = Office.componentDisabled
  val packageInformationProvider = O.I[XPackageInformationProvider](ctx.getValueByName("/singletons/com.sun.star.deployment.PackageInformationProvider"))
  val extensionLocation = packageInformationProvider.getPackageLocation(Extension.name)
  initialize(Array()) // initialized by default
  logger.info(componentName + " active")
  class XStorageCreateDialog extends XDialogEventHandler {
    def callHandlerMethod(xDialog: XDialog, aEventObject: Any, sMethodName: String): Boolean = {
      if (sMethodName == "external_event") {
        println("a")
/*              try {
                  return handleExternalEvent(xWindow, aEventObject);
              } catch (com.sun.star.uno.RuntimeException re) {
                  throw re;
              } catch (com.sun.star.uno.Exception e) {
                  throw new WrappedTargetException(sMethodName, this, e);
              }*/
      }
      true
    }
    def getSupportedMethodNames(): Array[String] = {
         return Array[String]("external_event")
    }
  }
  // ------------------------------
  // - implement trait XStorageUNO -
  // ------------------------------
  def create(storageName: String, storageLocation: String, user: XUserUNO): Long = {
    if (storageName.length == 0 || storageName == null)
      throw new RuntimeException("Illegal empty storage name")
    val handler = new XStorageCreateDialog()
    val dialogURL = extensionLocation + "/dialog/UserCreate.xdl"
    val id = create(storageName, storageLocation, user, dialogURL, handler)
    setID(id)
    id
  }
  def update(): Boolean = {
    val base = getURL().substring(7)
    val root = new JFile(base + getRoot.getPath())
    val baseLength = root.getPath().length
    logger.info("update storage '" + this.aName + "' at '" + base + "'")
    if (!root.exists()) {
      logger.info("create root folder " + root.getPath())
      root.mkdirs()
    }
    def iterate(parent: JFile, DEparent: XFolderUNO): Unit = {
      val folderContents : Array[JFile] = parent.listFiles
      folderContents.foreach(entity => {
          if (entity.isDirectory()) {
            val path = entity.getPath().substring(baseLength)
            logger.warn("check folder: " + path)
            val folder = core.getOrCreateFolder(this, path)
            folder.setParent(DEparent)
            iterate(entity, folder)
          } else {
            val path = entity.getPath().substring(baseLength)
            logger.warn("check file: " + entity.getName())
            val file = core.getOrCreateFile(DEparent, entity.getName)
          }
        })
    }
    iterate(root, getRoot())
    true
  }
  def ableToOverwriteURL(): Boolean = true
  def isInitialized() = true
  // -----------------------------------
  // - implement trait XInitialization -
  // -----------------------------------
  def initialize(args: Array[AnyRef]) {
    logger.info("initialize " + componentName)
  }
  // ------------------------------
  // - implement trait XComponent -
  // ------------------------------
  override def dispose() {
    logger.info("dispose " + componentName)
    super.dispose()
  }
}

object Office extends XBaseInfo {
  private val logger = LoggerFactory.getLogger(this.getClass.getName)
  var componentSingleton: Option[XBase] = None
  val componentTitle = "Documentum Elasticus Database"
  val componentDescription = "HSQL database component"
  val componentURL = "http://www."
  val componentName = classOf[Office].getName()
  val componentServices: Array[String] = Array(componentName)
  val componentDisabled = false
  logger.info(componentName + " active")
  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    println("Hello, world!")
  }
}

 /* // logic
  def update(): Boolean = {
    val oSimpleFileAccess = O.SI[XSimpleFileAccess](mcf, "com.sun.star.ucb.SimpleFileAccess", ctx)
    val basePath = aURL.getPath()
    val calendar = Calendar.getInstance()

    true
  }
  def init(tName: String, location: String) : Boolean = {
    val tURL = new java.net.URL(location)
    val oSimpleFileAccess = UnoHelper.queryServiceInterface[XSimpleFileAccess](mcf, "com.sun.star.ucb.SimpleFileAccess", ctx);
    if (!oSimpleFileAccess.exists(tURL.toString().replaceFirst("^file:/(?=[^/])","file:///"))) {
      val result = UI.showMessageBox("warningbox", com.sun.star.awt.MessageBoxButtons.BUTTONS_OK_CANCEL |
                                  com.sun.star.awt.MessageBoxButtons.DEFAULT_BUTTON_OK, "folder not found",
                                  "Do you want to create folder \"" +
                                  tURL.toString().replaceFirst("^file:/(?=[^/])","file:///") + "\"?", ctx)
      if (result == 1) {
        // RESULT_OK
        oSimpleFileAccess.createFolder(tURL.toString().replaceFirst("^file:/(?=[^/])","file:///"))
      } else {
        return false
      }
    }
    aURL = tURL
    aName = tName
    true
  }*/