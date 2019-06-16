package com.limdale.sponsortracker.model;

public enum SponsorshipWillingness {
    YES("yes"), NO("no"), MAYBE("maybe");

    private String label;

    SponsorshipWillingness(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static SponsorshipWillingness fromLabel(String label) {
        switch (label) {
            case "yes":
                return YES;
            case "no":
                return NO;
            case "maybe":
                return MAYBE;
            default:
                throw new IllegalStateException("Illegal sponsorship type: " + label);
        }
    }
    }
