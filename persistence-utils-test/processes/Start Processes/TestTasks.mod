[Ivy]
173A425FAB8461C1 9.3.1 #module
>Proto >Proto Collection #zClass
Ts0 TestTasks Big #zClass
Ts0 B #cInfo
Ts0 #process
Ts0 @TextInP .type .type #zField
Ts0 @TextInP .processKind .processKind #zField
Ts0 @TextInP .xml .xml #zField
Ts0 @TextInP .responsibility .responsibility #zField
Ts0 @StartRequest f0 '' #zField
Ts0 @EndTask f1 '' #zField
Ts0 @UserDialog f2 '' #zField
Ts0 @PushWFArc f4 '' #zField
Ts0 @PushWFArc f7 '' #zField
>Proto Ts0 Ts0 TestTasks #zField
Ts0 f0 outLink testTasks.ivp #txt
Ts0 f0 inParamDecl '<> param;' #txt
Ts0 f0 requestEnabled true #txt
Ts0 f0 triggerEnabled false #txt
Ts0 f0 callSignature testTasks() #txt
Ts0 f0 startName 'Execute test tasks' #txt
Ts0 f0 caseData businessCase.attach=true #txt
Ts0 f0 wfuser 1 #txt
Ts0 f0 @C|.xml '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<elementInfo>
    <language>
        <name>testTasks.ivp</name>
    </language>
</elementInfo>
' #txt
Ts0 f0 @C|.responsibility Administrator #txt
Ts0 f0 81 49 30 30 -21 17 #rect
Ts0 f1 337 49 30 30 0 15 #rect
Ts0 f2 dialogId com.axonivy.utils.persistence.test.TestTasks #txt
Ts0 f2 startMethod start() #txt
Ts0 f2 requestActionDecl '<> param;' #txt
Ts0 f2 responseMappingAction 'out=in;
' #txt
Ts0 f2 @C|.xml '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<elementInfo>
    <language>
        <name>TestTasks</name>
    </language>
</elementInfo>
' #txt
Ts0 f2 168 42 112 44 -29 -8 #rect
Ts0 f4 280 64 337 64 #arcP
Ts0 f7 111 64 168 64 #arcP
>Proto Ts0 .type com.axonivy.utils.persistence.test.Data #txt
>Proto Ts0 .processKind NORMAL #txt
>Proto Ts0 0 0 32 24 18 0 #rect
>Proto Ts0 @|BIcon #fIcon
Ts0 f2 mainOut f4 tail #connect
Ts0 f4 head f1 mainIn #connect
Ts0 f0 mainOut f7 tail #connect
Ts0 f7 head f2 mainIn #connect
