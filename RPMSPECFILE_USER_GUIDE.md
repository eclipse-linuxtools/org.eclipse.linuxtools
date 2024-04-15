Overview
========

The Specfile Editor Plug-in for Eclipse provides useful features to help developers manage *`.spec`* files. This plug-in allows users to leverage several Eclipse GUI features in editing *`.spec`* files, including outline/quick outline view in the editor, auto-completion, highlighting, file hyperlinks, and folding.

In addition, the Specfile Editor Plug-in also integrates the `rpmlint` tool into the Eclipse interface. `rpmlint` is a command-line tool that helps developers detect common RPM package errors. The richer visualization offered by the Eclipse interface helps developers quickly detect, view, and correct mistakes reported by `rpmlint`.

RPM plugin also supports the ability to create your own repository using the `createrepo` command. `createrepo` is a command-line tool that generates an xml-based RPM metadata repository from a set of RPMs. Eclipse offers this tool bundled with the RPM plugin to allow developers to create repositories in the same environment that they create their RPMs.

Installing
==========

In order for the Specfile Editor Plug-in for Eclipse to work, you should have the *`rpmlint`* package installed.

Once the *`rpmlint`* package is installed, the easiest way to install the Specfile Editor plug-in for Eclipse is through the ***Software Updates and Add-ons*** menu. For information on how to use this menu, refer to <https://wiki.eclipse.org/Linux_Tools_Project/PluginInstallHelp>.

The `createrepo` package needs to be installed for the `createrepo` features to work as intended. Specfile Editor Plug-in for Eclipse will continue to work even without the `createrepo` command not installed. However, if installed, do note that a `yum` version of 3.2.23 or higher is required to use the command to its full extent.

General Usage
=============

To fully benefit from all the features offered by the Specfile Editor, ensure that your *`.spec`* file is part of a project inside the Eclipse workspace. `rpmlint` integration, file hyperlinks, and some auto-completion features are not available otherwise. The following screenshot provides an impression of the full interface benefits provided by the Specfile Editor plug-in:
![](images/Specfile_generic.png "fig:Specfile_generic.png")

Creating a New RPM project
--------------------------

To create a RPM Project, user must go to File \> New \> Other, expand RPM and select RPM Project. In the next window, it is necessary to select the project name and the layout to be used. RPMBUILD layout means that the directory layout of BUILD, RPMS, SOURCES, SPECS and SRPMS is going to be used. FLAT layout means that all files will be kept inside the main project directory.

It is possible to use the default file system or create a RPM project remotely. To create the remote project, user must clear the Use default location check box and select RSE or RemoteTools. The Browse button can be used to find the location of where the project is to be stored on the remote server.

![](images/RPM_NewProject.png "RPM_NewProject.png")

Creating a New Specfile
-----------------------

The Specfile Editor plug-in provides a wizard for creating new *`.spec`* files. To use this wizard, navigate first to <u>File</u> \> <u>New</u> \> <u>Other...</u> ; then, expand the <u>RPM Wizards</u> entry to select <u>New specfile based on a template</u>.

![](images/Specfile_new_wizard.png "Specfile_new_wizard.png")

This will open the Specfile Creation wizard, which provides an interface for generating the basic contents of a *`.spec`* file.

![](images/Specfile_new_from_template.png "Specfile_new_from_template.png")

The Specfile Creation wizard contains the following fields:

Project  
This field associates the generated *`.spec`* with a project in the current workspace, which ultimately specifies where the *`.spec`* will be saved. The ***Select a project...*** button allows you to select a specific project for this field with ease. By default, the Specfile Editor plug-in specifies the last active project when you invoked the Specfile Creation wizard.

Select a template  
This drop-down list allows you to select a *`.spec`* template to use. You can install templates provided by `rpmdevtools`, which is available <https://fedorahosted.org/rpmdevtools> .

Version  
This fills in the `Version:` line of the *`.spec`* file.

Summary  
This fills in the `Summary:` line of the *`.spec`* file.

Group  
This drop-down list allows you to select a package group to which the project belongs.

License  
This fills in the `License:` line of the *`.spec`* file.

URL  
This fills in the `URL:` line of the *`.spec`* file, which typically specifies the public home page of the project.

Source0  
This field specifies the source archive from which the package is built.

Building RPMs
-------------

To build RPMs, right-click on the RPM project or the Specfile Editor. Then, select <u>RPM</u> menu item from the context menu. You will be given a list of build options for the *`.spec`* file, as well as other Specfile options.

![](images/SpecfileEditor_rpmMenu.png "SpecfileEditor_rpmMenu.png")

#### Toolbar

These build options are also readily available on the toolbar menu for quicker access.

![](images/SpecfileEditor_rpmToolbar.png "SpecfileEditor_rpmToolbar.png")

rpmlint
-------

To enable `rpmlint` warnings, right-click on the project containing the *`.spec`* file. Then, select the <u>RPM</u> menu item. Then, <u>Add/Remove rpmlint warnings</u> from the context menu. You can use this menu selection to disable `rpmlint` warnings as well.

![](images/Specfileeditor_rpmlint.png "Specfileeditor_rpmlint.png")

Enabling `rpmlint` warnings will add a new `rpmlint` builder to the project. This `rpmlint` builder checks the *`.spec`* file during each project build or clean. Afterwards, the builder displays any appropriate warnings and errors in the ***Problems*** pane. Each warning/error is plotted in the *`.spec`* file as well; clicking an `rpmlint` warning/error in the ***Problems*** pane will automatically place the insertion point on the corresponding section in the *`.spec`* file.

![](images/Specfile_problems_rpmlint.png "Specfile_problems_rpmlint.png")

### Quick Fixes

You can also directly resolve several warnings and errors through the ***Quick Fix*** menu. To access this menu, right-click on a warning or error from the ***Problems*** pane and select <u>Quick Fix</u>.

As the name suggests, the ***Quick Fix*** menu provides you with quick solutions to common *`.spec`* file errors and problems. For example, `rpmlint` detected a `no-cleaning-of-buildroot` error in the following `%install` section from the `.spec` file:

![](images/Specfile_rpmlint_fix_before.png "Specfile_rpmlint_fix_before.png")

If the ***Quick Fix*** menu can provide a solution for a particular error, it will be available in the menu. To apply it, simply select the solution (from the ***Select a fix:*** area) and the corresponding problem (from the ***Problems:*** area), then click the <u>Finish</u> button.

![](images/Specfile_rpmlint_quickfix.png "Specfile_rpmlint_quickfix.png")

The following screenshot shows the revisions to the `%install` section applied by the ***Quick Fix*** menu:

![](images/Specfile_rpmlint_fix_after.png "Specfile_rpmlint_fix_after.png")

Hyperlink Detection
-------------------

A common feature in most eclipse plugins is the ability to detect hyperlinks and to provide options for the user to handle the clicking of the hyperlink.

The SpecfileEditor handles the hyperlinks, often located in the *`SOURCE`* and *`PATCH`* defines, differently than regular hyperlinks.

As shown below you are given the options to:

-   Open the hyperlink in a browser
-   Open the file if it exists within the project (hidden if it doesn't)
-   Download the file from the browser

In addition to these, if the user also happens to hover above a macro (e.g. *`%{name}`* or *`%{version}`*), it would show a "Go to ..." option to jump to where the macro is defined, if located in the *`.spec`* file.

![](images/SpecfileEditor_hyperlinksAfter.png "SpecfileEditor_hyperlinksAfter.png")

#### Patches

Patches are also considered hyperlinks in the SpecfileEditor. Based on the link itself, patch options could show a combination of the following options:

-   Open the patch file in a browser if it is a URL
-   Create the file if it doesn't exist (or Open it if it does)
-   Download the file if it is a URL

![](images/SpecfileEditor_patchSourceImprovement.png "SpecfileEditor_patchSourceImprovement.png")

Import src.rpm
==============

One of the features of the RPM plugin is the ability to import src.rpm files into an Eclipse project. A selection has been added to the screen that is displayed when the Eclipse File \> Import \> Other pulldown item is selected. This allows you to quickly and easily import source RPMs into the Eclipse development environment.

Invoking the Eclipse SRPM Import Feature
----------------------------------------

Invoking the SRPM Import feature of Eclipse is a very simple matter. At the top of the Eclipse click on File which causes a pulldown menu to appear. From this menu click on Import. This causes the screen below to appear:

![](images/Specfile import srpm.png "Specfile import srpm.png")

From this screen select Source RPM and then Next to activate the next screen shown here:

![](images/Specfile_import_select_srpm.png "Specfile_import_select_srpm.png")

There are three functions that this screen must perform in order to successfully import a source RPM into Eclipse. The first thing you must do is select a source RPM to be imported using the Browse button. It is used to browse around a system to the desired source RPM. Once the desired source RPM is found, either double-click on it or single-click and then click OK. The source RPMs name should now appear in the SRPM Name window.

Next, either select the already-existing project where this source RPM will be imported or click on the "Check out as a project using the New Project Wizard" button. If you select the latter, the New project wizard pops up and guides you through the process of creating a new project. Once the wizard is done, the import process begins automatically. If there is a project already created you want to import into, the Select a project window contains a list of the available Eclipse projects. Scroll to the desired project and select it so it is highlighted and click on "Finish".

Now you can use all of the resources of Eclipse on this imported project just as any other project. Notice that the source RPM that was selected for import has been copied into the project.

Configuring Specfile Editor Settings
====================================

To configure Specfile Editor settings, navigate to <u>Window</u> / <u>Preferences</u>. Then, from the left pane of the ***Preferences*** menu, select ***Specfile Editor***.

![](images/Specfile_settings_changelog.png "Specfile_settings_changelog.png")

Upon selecting the ***Specfile Editor*** menu, you can configure the format and locale of Changelog entries. To edit the actual content of your Changelogs, click the <u>ChangeLog</u> hyperlink in the menu, or use **Ctrl**+**Alt**+**C** (outside of the ***Preferences*** menu).

Macro Completion
----------------

Macro definitions enable the Specfile Editor to properly highlight and autocompile *.spec* files. The ***Macro Completion*** sub-menu allows you to add/remove macro definitions and configure how the Specfile Editor should present macro values during mouse hovers.

![](images/Specfile_settings_macros.png "Specfile_settings_macros.png")

The ***Macro Definitions*** section shows what macros the Specfile Editor is using. You can add, delete, and reorder macros using the buttons to the left of this section.

The ***Mouse hover content*** section enables you to specify what kind of information about a macro value whenever you hover your mouse over it. You can choose whether to display the contents or description of a macro value. The following screenshots illustrate the difference between both options:

Show macro descriptions  
![](images/Specfile_macros_description.png "fig:Specfile_macros_description.png")

Show macro contents  
![](images/Specfile_macros_content.png "fig:Specfile_macros_content.png")

RPM Completions
---------------

The ***RPM Completions*** section allows you to configure generic package list settings. This section allows you to use distribution-specific references which supplement auto-completion in editing *`.spec`* files. These distribution-specific references include a list of installed RPM packages as well as online repositories.

![](images/Specfile_settings_rpms_generic.png "Specfile_settings_rpms_generic.png")

***RPM Completions*** configures the following settings:

-   Whether to use `rpm` or `yum` to build the package list
-   The path to the package list file
-   Whether or not to automatically build the RPM packages proposals list
-   When to refresh package list (***Proposals RPM list build rate***)

The Specfile Editor can use package management tools to build the package list. Currently, the Specfile Editor only supports `yum` and `urpmi`. If neither package management tools are present in the system, the Specfile Editor will use `rpm` to build the package list.

### Package Information

This sub-section of ***RPM Completions*** allows you to configure what package details (i.e. RPM tags) to display during mouse hovers over appropriate values. ***Package Information*** also lets you set the number of packages to show details for.

![](images/Specfile_settings_rpms_details.png "Specfile_settings_rpms_details.png")

***Package Information*** contains a checklist of RPM tags (e.g. ***Name***, ***Version***, ***Release***). The Specfile Editor displays these tags during mouse hovers on package information in the *`.spec`* file. You can also set a threshold for the number of proposals that enables the Specfile Editor to display this information. By default, the threshold is 10.

Rpmlint
-------

The ***Rpmlint*** section allows you to configure where `rpmlint` is installed, which is useful if it is installed somewhere other than the default *`/usr/bin/rpmlint`*. This section also allows you to set whether warnings should be used for tabs or spaces, which is useful if you use both in the *`.spec`* file.

![](images/Specfile_settings_rpmlint.png "Specfile_settings_rpmlint.png")

Task Tags
---------

The ***Task Tags*** section allows the Specfile Editor to identify specific strings as *task tags*. The Specfile Editor will only search for these strings in comments. By default, the Specfile Editor identifies the strings `TODO` and *`FIXME`* as task tags.

![](images/Specfile_settings_tasks.png "Specfile_settings_tasks.png")

Whenever the Specfile Editor finds a task tag in the *`.spec`* file, it adds the tag to the ***Tasks*** view and plots an appropriate marker in the editor. Clicking the tag in the ***Tasks*** view will automatically place the insertion point on the corresponding string in the *`.spec`* file.

![](images/Screenshot-taskscreenshot.png "Screenshot-taskscreenshot.png")

Templates
---------

The ***Templates*** section allows you to share and manage different *`.spec`* file directive/section templates for all contexts.

![](images/Specfile_settings_templates.png "Specfile_settings_templates.png")

The <u>Import</u> and <u>Export</u> functions allow you to share templates with other developers who may also be working on the same package. The <u>New</u> and <u>Edit</u> buttons open a menu for adding or revising templates; they both contain the same options.

![](images/Screenshot-edittemplate.png "Screenshot-edittemplate.png")
