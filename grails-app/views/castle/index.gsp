<%@ page import="grails.converters.JSON" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'castle.label', default: 'Castle')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
    <link rel="stylesheet" href="${createLink(uri: '/webjars/leaflet/1.9.4/dist/leaflet.css')}"/>
    <script src="${createLink(uri: '/webjars/leaflet/1.9.4/dist/leaflet.js')}"></script>
    <style>#map { height: 420px; }</style>
</head>
<body>
<div id="content" role="main">
    <div class="container">
        <section class="row">
            <a href="#list-castle" class="visually-hidden-focusable" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
            <nav class="navbar navbar-expand-lg bg-body-tertiary">
                <ul class="navbar-nav container-fluid">
                    <li class="nav-item"><a class="nav-link btn" aria-label="Home" href="${createLink(uri: '/')}">
                        <i class="bi-house"></i> <g:message code="default.home.label"/></a>
                    </li>
                    <li class="nav-item me-lg-auto">
                        <g:link class="nav-link btn" aria-label="List" action="create"><i class="bi-database-add"></i> <g:message code="default.new.label" args="[entityName]" /></g:link>
                    </li>
                </ul>
            </nav>
        </section>
        <section class="row">
            <div id="list-${propertyName}" class="col-12 content scaffold-list" role="main">
                <h1>
                    <g:message code="default.list.label" args="[entityName]" /></h1>
                <g:flashMessages />

                <div id="map" class="mb-3 border rounded"></div>

                <f:table class="scaffold table table-striped table-sm" controller="${controllerName}" collection="${castleList}"/>

                <g:if test="${castleCount > params.int('max')}">
                    <div class="btn-toolbar mb-3" role="toolbar" aria-label="Toolbar with button groups">
                        <g:paginate activeClass="active" class="btn" total="${castleCount ?: 0}" />
                    </div>
                </g:if>
            </div>
        </section>
    </div>
</div>
<script>
    const markers = ${raw((markers as JSON).toString())};
    // Wait for the stylesheets: Leaflet needs the container's final size to fit the bounds.
    window.addEventListener('load', () => {
        const map = L.map('map');
        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 18,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);
        const group = L.featureGroup(markers.map(m =>
            L.marker([m.lat, m.lng]).bindPopup(
                `<strong><a href="${'$'}{m.url}">${'$'}{m.name}</a></strong><br>${'$'}{m.city}<br>${'$'}{m.knights} knight(s)`)
        )).addTo(map);
        if (markers.length) {
            map.fitBounds(group.getBounds().pad(0.2));
        } else {
            map.setView([56.19, -4.05], 6);
        }
    });
</script>
</body>
</html>
