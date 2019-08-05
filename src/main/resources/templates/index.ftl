<html>
<head>
</head>
<body>
<h1><p>Conferences</p></h1>
<ul>

    <#if conferences??>
        <#list conferences as confKey, confValue>

            <li>Name: ${confKey}
                <ul>
                    <li>Status: ${confValue.getSpec().getStatus()}</li>
                    <li>Location: ${confValue.getSpec().getLocation()}</li>
                    <li>Year: ${confValue.getSpec().getYear()}</li>
                    <li>URL: http://${confValue.getSpec().getUrl()}</li>
                    <li>Modules:
                        <ul>
                            <#if confValue??>
                                <#list confValue.getSpec().getModules() as module>
                                    <li>Module: ${module.getName()}
                                        <ul>
                                            <li>Module
                                                Version: ${service.getPipelineActivityLastVersionForModule(confKey, module.getName())}</li>
                                            <li>Pipeline
                                                Status: ${service.getPipelineActivityLastStatusForModule(confKey, module.getName())}</li>
                                            <li>Service
                                                Status: OK
                                            </li>
                                        </ul>
                                    </li>
                                <#else>
                                    <p>No Modules.</p>
                                </#list>
                            </#if>
                        </ul>
                    </li>
                </ul>
            </li>

            </li>
        <#else>
            <p>You don't have any conferences yet.</p>
        </#list>
    </#if>
</ul>
<script type="text/javascript">
    $(document).ready(function() {
        setInterval(function() {
            window.location=window.location;
        }, 3000);
    });
</script>
</body>
</html>