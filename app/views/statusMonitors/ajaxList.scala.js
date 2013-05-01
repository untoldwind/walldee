@(statusMonitors: Map[StatusMonitorTypes.Type, Seq[StatusMonitor]])

$("#statusMonitor-list").html("@views.html.statusMonitors.list(statusMonitors)");