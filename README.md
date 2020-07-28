<a href="https://www.jahia.com/">
    <img src="https://www.jahia.com/modules/jahiacom-templates/images/jahia-3x.png" alt="Jahia logo" title="Jahia" align="right" height="60" />
</a>

JCR OAuth Provider
======================

This module works with Jahia OAuth and allows you to create(if needed) and connect an user as a result of an open authentication.

### MINIMAL REQUIREMENTS
* Jahia 8.0.0.0
* Jahia OAuth module 2.0.0
* At least one connector module

### INSTALLATION
Download the jar and deploy it on your instance then activate the module on the site you wish to use.

### WHAT DOES THIS MODULE DO?
It brings the possibility to create a mapping between the data from a connector and the property of JCR user.  
It creates a JCR user if the user does not exist yet.  
It connects the user as a result of the open authentication.

### HOW TO USE IT?
Once you have downloaded Jahia OAuth and at least one connector:
* Go to your `site > site settings > Jahia OAuth`
* In the panel you will see the list of connectors that are available for your site and if you open the card you will see the parameters to fill in order to activate and use it
* You will need to go to the open authentication website and setup an app to get the parameters. Please refer to the documentation on their website
* Once this is done a new button will appear `Actions` and if you click on it you will access to the action modules part
* On this part you can activate as many action modules type mapper as you which but you can only activate one provider
* Create a mapping for the JCR OAuth Provider
* Then in edit mode add the connection button of your connector to a page  
* Publish your site
* Your users can now connect using open authentication


## Open-Source

This is an Open-Source module, you can find more details about Open-Source @ Jahia [in this repository](https://github.com/Jahia/open-source).

