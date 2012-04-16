*Step 1 - Add the plugin to your DITA OT installation*
****************************************************************
    You can download the latest DITA-NG distribution to get the Relax NG support as a DITA-OT plugin
        dita-ngVERSION.zip 
    or you can checkout the DITA-NG project then run
        ant clean
        ant
    The plugin will be packed as an archive in the dist subfolder
        dita-ngVERSION.zip
    (VERSION is the plugin version number using the YYYYMMDD format, YYYY=year, MM=month and DD=day).

    Extract the archive in the plugins folder of your DITA-OT installation. 
    The result should be a RelaxNG folder inside your DITA-OT plugins folder.

*Step 2 - Integrate the plugin*
****************************************************************
    Change the startcmd.sh and startcmd.bat scripts to add the dita-ng.jar and jing.jar in the classpath by adding them to NEW_CLASSPATH:
        NEW_CLASSPATH="$DITA_DIR/plugins/RelaxNG/lib/dita-ng.jar:$DITA_DIR/plugins/RelaxNG/lib/jing.jar:$NEW_CLASSPATH"

    Make sure DITA_HOME is set. For example you can set that running 
        export DITA_HOME=.
    in your actual DITA home folder.
    Run the startcmd script
        ./startcmd.sh
    Run
        ant -f integrator.xml

*Step 3 - Check that everything works*
****************************************************************
    Run a transformation to make sure everything is working. 
    For example, to generate XHTML from the flowers sample use: 
        ant -f build.xml -Dargs.input=plugins/RelaxNG/demo/flowers/flowers.ditamap -Doutput.dir=plugins/RelaxNG/demo/flowers/out -Dtranstype=xhtml
    The result will be in plugins/RelaxNG/demo/flowers/out/index.html
    To generate PDF from the flowers sample use: 
        ant -f build.xml -Dargs.input=plugins/RelaxNG/demo/flowers/flowers.ditamap -Doutput.dir=plugins/RelaxNG/demo/flowers/out -Dtranstype=pdf
    The result will be in plugins/RelaxNG/demo/flowers/out/flowers.pdf
    