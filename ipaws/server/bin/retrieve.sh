includeScriptLibraries arguments

readonly defaultServerType="prod"
readonly defaultMinutesBefore=5

addCommandOption e getEmergencyAlerts flag "get EAS [Emergency Alert System] alerts"
addCommandOption g getGeneralAlerts flag "get general (non-EAS) alerts"
addCommandOption m minutesBefore minutes "how far before the reference time to look" "${defaultMinutesBefore}"
addCommandOption p requestPIN characters "the server access PIN"
addCommandOption s serverType type "which server to use" "${defaultServerType}"

showRetrieveUsageSummary() {
cat <<END-OF-RETRIEVE-USAGE-SUMMARY

Alert type options can be combined. If none are specified then all are assumed.
If more than one server type option is specified then the last one is used.

Successfully retrieved alerts files are in the data directory. They're
named <time>.${alertsFileExtension}, where <time> is the ISO-8601 UTC timestamp
(yyyy-mm-ddThh:mm:ssZ) for when the file was retrieved.

The reference time used for alerts retrieval is the name of the most recently
retrieved alerts file (*.${alertsFileExtension}). If there aren't any then the current time is
used. The file extension .${failureFileExtension} indicates a failure during alerts retrieval.

If the PIN isn't specified then it's read from *.pin in the configuration
directory, where * is the server type (see *-server in the configuration file).
END-OF-RETRIEVE-USAGE-SUMMARY
} && readonly -f showRetrieveUsageSummary

