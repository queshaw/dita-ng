*Step 1 - get/build a distribution*
****************************************************************
    You can download the latest DITA-NG distribution
        dita-ngVERSION.zip 
    or you can checkout the DITA-NG project then run
        ant clean
        ant
    to get the Relax NG support as a DITA-OT plugin. The plugin will be packed as an archive in the dist subfolder
        dita-ngVERSION.zip
    VERSION is the plugin version number using the YYYYMMDD format (YYYY=year MM=month DD=day).

*Step 2*
****************************************************************
    Extract the archive in the plugins folder of a DITA-OT installation. 
    The result should be a RelaxNG folder inside your DITA-OT plugins folder.

*Step 3*
****************************************************************
    Change the startcmd.sh and startcmd.bat scripts to add the dita-ng.jar and jing.jar in the classpath by adding them to NEW_CLASSPATH:
        NEW_CLASSPATH="$DITA_DIR/plugins/RelaxNG/lib/dita-ng.jar:$DITA_DIR/plugins/RelaxNG/lib/jing.jar:$NEW_CLASSPATH"

*Step 4*
****************************************************************
    Make sure DITA_HOME is set. For example you can set that running 
        export DITA_HOME=.
    in your actual DITA home folder.
    Run the startcmd script
        ./startcmd.sh

*Step 5*
****************************************************************
    Run a transformation to make sure everything is working: 
        ant -f build.xml -Dargs.input=plugins/RelaxNG/demo/flowers/flowers.ditamap -Doutput.dir=plugins/RelaxNG/demo/flowers/out -Dtranstype=xhtml -Dvalidate=false
    
    