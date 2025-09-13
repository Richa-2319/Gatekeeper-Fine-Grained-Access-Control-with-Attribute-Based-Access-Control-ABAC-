package gatekeeper.authz

import rego.v1

# Default deny
default allow := false

# Business hours policy
allow if {
    business_hours
}

# Admin always allowed
allow if {
    input.user.role == "admin"
}

# Department-based access
allow if {
    input.user.department == input.context.department
    not sensitive_resource
}

# Time-based access control
business_hours if {
    now := time.now_ns()
    date := time.date(now)
    hour := date[3]  # Hour component
    hour >= 9
    hour <= 17
}

# Sensitive resource check
sensitive_resource if {
    contains(input.resource, "sensitive")
}

# Location-based access
allow if {
    input.user.location == "office"
    not sensitive_resource
}

# Read access for regular users during business hours
allow if {
    input.action == "read"
    not sensitive_resource
    business_hours
}