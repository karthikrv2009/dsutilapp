#!/bin/bash

# Function to calculate the validity date
calculate_validity() {
  local license_type=$1
  if [ "$license_type" == "trail" ]; then
    validity_date=$(date -v +7d +"%Y-%m-%d" 2>/dev/null || date -d "+7 days" +"%Y-%m-%d")
  else
    validity_date=$(date -v +1y +"%Y-%m-%d" 2>/dev/null || date -d "+1 year" +"%Y-%m-%d")
  fi
  echo $validity_date
}

# Function to generate a 16-digit alphanumeric license key
generate_license_key() {
  local license_key=$(LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c 16)
  echo $license_key
}

# Function to generate the license
generate_license() {
  local company_name=$1
  local machine_name=$2
  local license_type=$3

  if [[ "$license_type" != "trail" && "$license_type" != "standard" && "$license_type" != "premium" ]]; then
    echo "Invalid license type. Please choose from 'trail', 'standard', or 'premium'."
    exit 1
  fi

  local validity=$(calculate_validity $license_type)
  local license_key=$(generate_license_key)

  echo "Debug: Validity calculated as $validity"  # Debugging statement
  echo "Debug: License key generated as $license_key"  # Debugging statement

  echo "Company Name: $company_name"
  echo "Machine Name: $machine_name"
  echo "License Type: $license_type"
  echo "Validity: $validity"
  echo "License Key: $license_key"
}

# Check if the correct number of arguments is provided
if [ "$#" -ne 3 ]; then
  echo "Usage: $0 <company_name> <machine_name> <license_type>"
  exit 1
fi

# Call the generate_license function with the provided arguments
generate_license "$1" "$2" "$3"