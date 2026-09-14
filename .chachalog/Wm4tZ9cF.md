---
# Allowed version bumps: patch, minor, major
jcr-auth-provider: major
---

Changed the JCR mapper so it matches an account on the identity the provider asserted. The mapper records that identity on the account it creates, and it reads the account of a login from that record.

Two conditions may meet: the record matches no account, and an account of the login's name already exists. The mapper then creates nothing, and it reports that the existing account has to be linked.
