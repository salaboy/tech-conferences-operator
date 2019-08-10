<html>
<!doctype html>
<html class="no-js" lang="">

<head>
    <meta charset="utf-8">
    <title></title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="manifest" href="site.webmanifest">
    <link rel="apple-touch-icon" href="icon.png">
    <!-- Place favicon.ico in the root directory -->
    <link rel="stylesheet" href="css/normalize.css">
    <link rel="stylesheet" href="css/main.css">
    <link href="https://fonts.googleapis.com/css?family=Rubik:400,500&display=swap" rel="stylesheet">


    <meta name="theme-color" content="#fafafa">
</head>

<body class="dashboard">
<!--[if IE]>
<p class="browserupgrade">You are using an <strong>outdated</strong> browser. Please <a href="https://browsehappy.com/">upgrade your browser</a> to improve your experience and security.</p>
<![endif]-->

<div class="dashboard-title">
    <h1 class="main-title">Conferences Dashboard</h1>
</div>
<div class="dashboard-content">
    <ul class="conference-list">
        <#if conferences??>
          <#list conferences as confKey, confValue>
                <#if confValue.getSpec().getStatus() == "HEALTHY">
                <li class="conference-item healthy">
                <#elseif confValue.getSpec().getStatus() == "UNHEALTHY">
                    <li class="conference-item warning">
                <#else>
                    <li class="conference-item error">
                </#if>
                    <h2 class="conference-item__title">
                        <a href="http://${confValue.getSpec().getUrl()}" target="_blank">
                            ${confKey}
                        </a>
                    </h2>
                    <div class="flag"></div>
                    <ul class="conference-item__info">
                        <#if confValue.getSpec().getStatus() == "UNHEALTHY">
                        <li class="conference-item__info__status"><span><img src="img/status-warning.svg">
                        <#elseif confValue.getSpec().getStatus() == "DOWN">
                        <li class="conference-item__info__status"><span><img src="img/status-error.svg">
                        <#else>
                        <li class="conference-item__info__status"><span><img src="img/status-healthy.svg">
                        </#if>
                                </span>Status: ${confValue.getSpec().getStatus()}
                        </li>
                        <li class="conference-item__info__location"><span><img src="img/location.svg"></span>Location: ${confValue.getSpec().getLocation()}</li>
                        <li class="conference-item__info__location"><span><img src="img/calendar.svg"></span>Year: ${confValue.getSpec().getYear()}</li>

                        <li>
                            <ul class="conference-item__modules">
                                <#list confValue.getSpec().getModules() as module>

                                    <#if (service.getPipelineActivityLastStatusForModule(confKey, module.getName()))?has_content
                                    && service.getPipelineActivityLastStatusForModule(confKey, module.getName()) == "Failed"  >
                                        <li class="conference-item__module warning">
                                    <#elseif service.getModuleServiceStatus(confKey, module.getName())?has_content && service.getModuleServiceStatus(confKey, module.getName()) == "DOWN">
                                        <li class="conference-item__module error">
                                        <#else>
                                        <li class="conference-item__module healthy">
                                    </#if>
                                        <div class="conference-item__module__flag"></div>
                                        <div class="conference-item__module__title"></i> ${module.getName()} <a href="http://github.com/salaboy/${module.getName()}" target="_blank"><i class="fa fa-github"></i></a></div>
                                        <ul>
                                            <li class="conference-item__module__item">Version: <span> ${(service.getPipelineActivityLastVersionForModule(confKey, module.getName()))!"N/A"}</span> <a href="http://github.com/salaboy/${module.getName()}/releases/tag/v${(service.getPipelineActivityLastVersionForModule(confKey, module.getName()))!"N/A"}" target="_blank"><i class="fa fa-github"></i></a></li>
                                            <li class="conference-item__module__item">Pipeline Status: <span>${(service.getPipelineActivityLastStatusForModule(confKey, module.getName()))!"N/A"}</span></li>
                                            <li class="conference-item__module__item">Service Status: <span>${(service.getModuleServiceStatus(confKey, module.getName()))!"N/A"}</span></li>
                                        </ul>
                                    </li>
                                <#else>
                                    <p>No Modules.</p>
                                </#list>
                            </ul>
                        </li>
                    </ul>
                </li>

          <#else>
              <p>You don't have any conferences yet.</p>
          </#list>
        </#if>
    </ul>
</div>

<script src="js/vendor/modernizr-3.7.1.min.js"></script>
<script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=" crossorigin="anonymous"></script>
<script>window.jQuery || document.write('<script src="js/vendor/jquery-3.4.1.min.js"><\/script>')</script>
<script src="js/plugins.js"></script>
<script src="js/main.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
        setInterval(function() {
            window.location=window.location;
        }, 5000);
    });
</script>

</body>

</html>