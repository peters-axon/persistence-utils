[Ivy]
17F21BE8CC94A13E 9.3.1 #module
>Proto >Proto Collection #zClass
Ts0 TestTasksProcess Big #zClass
Ts0 RD #cInfo
Ts0 #process
Ts0 @TextInP .type .type #zField
Ts0 @TextInP .processKind .processKind #zField
Ts0 @TextInP .xml .xml #zField
Ts0 @TextInP .responsibility .responsibility #zField
Ts0 @UdEvent f3 '' #zField
Ts0 @UdExitEnd f4 '' #zField
Ts0 @PushWFArc f5 '' #zField
Ts0 @UdEvent f18 '' #zField
Ts0 @UdProcessEnd f19 '' #zField
Ts0 @GridStep f21 '' #zField
Ts0 @PushWFArc f22 '' #zField
Ts0 @PushWFArc f20 '' #zField
Ts0 @UdInit f0 '' #zField
Ts0 @UdProcessEnd f1 '' #zField
Ts0 @PushWFArc f2 '' #zField
>Proto Ts0 Ts0 TestTasksProcess #zField
Ts0 f3 guid 171544322A1E7FF4 #txt
Ts0 f3 actionTable 'out=in;
' #txt
Ts0 f3 @C|.xml '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<elementInfo>
    <language>
        <name>close</name>
    </language>
</elementInfo>
' #txt
Ts0 f3 83 243 26 26 -15 15 #rect
Ts0 f4 211 243 26 26 0 12 #rect
Ts0 f5 109 256 211 256 #arcP
Ts0 f18 guid 172CC8F23D93EF68 #txt
Ts0 f18 actionTable 'out=in;
' #txt
Ts0 f18 @C|.xml '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<elementInfo>
    <language>
        <name>prepareTestEnvironment</name>
    </language>
</elementInfo>
' #txt
Ts0 f18 83 147 26 26 -68 15 #rect
Ts0 f19 403 147 26 26 0 12 #rect
Ts0 f21 actionTable 'out=in;
' #txt
Ts0 f21 actionCode 'import com.axonivy.utils.persistence.test.service.TestService;


in.message = TestService.prepareTestDataAndIvy(in.cleanReload);
' #txt
Ts0 f21 security system #txt
Ts0 f21 @C|.xml '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<elementInfo>
    <language>
        <name>Prepare test&#13;
environment</name>
    </language>
</elementInfo>
' #txt
Ts0 f21 224 138 112 44 -34 -16 #rect
Ts0 f22 109 160 224 160 #arcP
Ts0 f20 336 160 403 160 #arcP
Ts0 f0 guid 173A42862125166E #txt
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
Ts0 f1 403 51 26 26 0 12 #rect
Ts0 f2 109 64 403 64 #arcP
>Proto Ts0 .type com.axonivy.utils.persistence.test.TestTasks.TestTasksData #txt
>Proto Ts0 .processKind HTML_DIALOG #txt
>Proto Ts0 -8 -8 16 16 16 26 #rect
Ts0 f3 mainOut f5 tail #connect
Ts0 f5 head f4 mainIn #connect
Ts0 f18 mainOut f22 tail #connect
Ts0 f22 head f21 mainIn #connect
Ts0 f21 mainOut f20 tail #connect
Ts0 f20 head f19 mainIn #connect
Ts0 f0 mainOut f2 tail #connect
Ts0 f2 head f1 mainIn #connect
