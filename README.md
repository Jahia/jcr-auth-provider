# JCR OAuth Provider

### INFORMATION
This module work with Jahia OAuth and allow you to create(if needed) and connect an user as a result of an open authentication.

### MINIMAL REQUIREMENTS
* DX 7.2.0.0
* Jahia OAuth module 1.0.0
* At least one connector module

### INSTALLATION
Download the jar and deploy it on your instance then activate the module on the site you wish to use it.

### WHAT THIS MODULE DOES?
It bring the possibility to create a mapping between the data from a connector and the property of JCR user.  
It create a JCR user if the user doesn't exist yet.  
It connect the user as a result of the open authentication.

### HOW TO USE IT?
Once you have downloaded Jahia OAuth and at least one connector:
* go to your `site > site settings > Jahia OAuth`
* In the panel you will see the list of connectors that are available for your site and if you open the card you will see the parameters to fill in order to activate and use it
* You will need to go to the open authentication website and setup an app to get the parameters. Please refer to the documentation on their website
* Once this is done a new button will appear `Actions` and if you click on it you will access to the action modules part
* On this part you can activate as many action modules type mapper as you which but you can only activate one provider
* Create a mapping for the JCR OAuth Provider
* Then in edit mode add the connection button of your connector to a page  
* Publish you site
* Your users can now connect using open authentication