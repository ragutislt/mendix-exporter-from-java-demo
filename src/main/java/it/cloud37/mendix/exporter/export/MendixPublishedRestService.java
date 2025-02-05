package it.cloud37.mendix.exporter.export;

import java.util.List;

record MendixPublishedRestService(String serviceName, String path, String version, List<MendixPublishedRestServiceResource> resources) {
    
}
