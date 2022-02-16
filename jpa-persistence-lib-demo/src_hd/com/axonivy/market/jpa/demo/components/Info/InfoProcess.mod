[Ivy]
173BD7E18B4BE71C 9.3.1 #module
>Proto >Proto Collection #zClass
Ts0 InfoProcess Big #zClass
Ts0 RD #cInfo
Ts0 #process
Ts0 @TextInP .type .type #zField
Ts0 @TextInP .processKind .processKind #zField
Ts0 @TextInP .xml .xml #zField
Ts0 @TextInP .responsibility .responsibility #zField
Ts0 @UdInit f0 '' #zField
Ts0 @UdProcessEnd f1 '' #zField
Ts0 @PushWFArc f2 '' #zField
Ts0 @UdMethod f6 '' #zField
Ts0 @UdProcessEnd f7 '' #zField
Ts0 @GridStep f9 '' #zField
Ts0 @PushWFArc f10 '' #zField
Ts0 @PushWFArc f8 '' #zField
>Proto Ts0 Ts0 InfoProcess #zField
Ts0 f0 guid 173BD7E18F345B7F #txt
Ts0 f0 method start() #txt
Ts0 f0 inParameterDecl '<> param;' #txt
Ts0 f0 outParameterDecl '<> result;' #txt
Ts0 f0 @C|.xml '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<elementInfo>
    <language>
        <name>start()</name>
    </language>
</elementInfo>
' #txt
Ts0 f0 83 51 26 26 -16 15 #rect
Ts0 f1 211 51 26 26 0 12 #rect
Ts0 f2 109 64 211 64 #arcP
Ts0 f6 guid 173BD8B13D0CDB1F #txt
Ts0 f6 method showInfo(String) #txt
Ts0 f6 inParameterDecl '<String value> param;' #txt
Ts0 f6 inParameterMapAction 'out.value=param.value;
' #txt
Ts0 f6 outParameterDecl '<> result;' #txt
Ts0 f6 @C|.xml '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<elementInfo>
    <language>
        <name>showInfo(String)</name>
    </language>
</elementInfo>
' #txt
Ts0 f6 83 211 26 26 -45 15 #rect
Ts0 f7 339 211 26 26 0 12 #rect
Ts0 f9 actionTable 'out=in;
' #txt
Ts0 f9 actionCode 'import java.util.HashMap;
import java.util.Map;
import org.primefaces.PrimeFaces;

ivy.log.info(in.value);

Map options = new HashMap();
options.put("resizable", false);


PrimeFaces.current().dialog().openDynamic("InfoDialog", options, null);' #txt
Ts0 f9 @C|.xml '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<elementInfo>
    <language>
        <name>Show info</name>
    </language>
</elementInfo>
' #txt
Ts0 f9 168 202 112 44 -27 -8 #rect
Ts0 f10 109 224 168 224 #arcP
Ts0 f8 280 224 339 224 #arcP
>Proto Ts0 .type com.axonivy.market.jpa.demo.components.Info.InfoData #txt
>Proto Ts0 .processKind HTML_DIALOG #txt
>Proto Ts0 -8 -8 16 16 16 26 #rect
Ts0 f0 mainOut f2 tail #connect
Ts0 f2 head f1 mainIn #connect
Ts0 f6 mainOut f10 tail #connect
Ts0 f10 head f9 mainIn #connect
Ts0 f9 mainOut f8 tail #connect
Ts0 f8 head f7 mainIn #connect
