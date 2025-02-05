package it.cloud37.mendix.exporter.export;

import java.util.List;

record MendixPublishedRestServiceResource(String name, List<MendixPublishedRestServiceOperation> operations) {

}
