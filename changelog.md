------------------------------------------------------
Numismatics 1.1.0
------------------------------------------------------
Additions
- Computer Craft: Tweaked compat for vendors, brass depositors, salepoint's and bank terminals
- Common tag that all blocks have with another tag for items to make it easier to allow use of numismatics blocks/items with claims
- Config option to select the default coin reference type to be used in UIs (default is suns & cogs)
- Config option to pick how much money each player should receive the first time they open their bank account
- /payall command to pay all users
- Toggle button to enable/disable item extraction from vendors
- Bulk buy/sell up to a stack of items to vendors by sneaking while using the vendor
- Add Sub Accounts: created in the Bank Terminal, these allow binding Authorized Cards with spending limits and separate trust lists, drawing from and depositing to the parent Bank Account
- Salepoint: Similar to the vendor, but allows players to queue a transaction made up of multiple purchases through a Portable Item/Fluid/Energy Interface

Fixes
- Fix coins stacked above 127 visually disappearing on the client in Brass Depositors
- Texture inconsistency in the gui texture of blaze terminals, blaze bankers and trust lists
- Creative vendors can no longer be modified by players in survival

Changes
- Allow rebinding the break keybind using the crouch keybind (was previously left shift + break key)
- Redid Bank Terminal texture to match other Create textures
- You can now insert or extract items from all sides of a vendor
- The buying/selling item slot in the vendor gui now acts like a ghost item (does not require actual items).
  Backwards compatible with existing vendors, so the items will remain 'real' until you remove them.
  Additionally, enchantments and dyes can be applied to some items by shift-dragging items when using EMI on fabric or JEI on forge
- OP'ed players no longer automatically have access to all Numismatics blocks. Instead, they can use the command `/numismatics toggle_admin_mode` to toggle that ability
- Update to Create 0.5.1-j
------------------------------------------------------
Numismatics 1.0.8
------------------------------------------------------
Fixes
- Update to Create Fabric 0.5.1-j
------------------------------------------------------
Numismatics 1.0.7
------------------------------------------------------
Fixes
- Double items being given when clicking a vendor with the offhand
------------------------------------------------------
Numismatics 1.0.6
------------------------------------------------------
Fixes
- Fix IllegalAccessError on forge
------------------------------------------------------
Numismatics 1.0.5
------------------------------------------------------
Fixes
- Bank terminals properly check authorization now
- Creative Vendor breakable in survival
- Numismatics blocks being explodable
- Vendor Menu cannot be opened when an item is in the offhand (#65)
- Fix coins stacked above 127 visually disappearing on the client in vendors (#72)
- Fix running pay, deduct and view commands on a non-existent blaze banker throwing exceptions (#63)
------------------------------------------------------
Numismatics 1.0.4
------------------------------------------------------
Additions
- Translations to many languages (Thank you Translators!)

Fixes
- Dupe
- Vendors not dropping items upon breaking
------------------------------------------------------
Numismatics 1.0.3
------------------------------------------------------
Additions
- Russian translation - Thanks VladisCrafter!

Fixes
- Empty blaze burners no longer become blaze bankers
- Being able to power redstone behind depositors by powering the depositor with a repeater
- Depositors taking money held in hand even when you have Insufficient funds
- Funnel/Chute vendor restocking not working on forge
- Translatability of coin names/values
------------------------------------------------------
Numismatics 1.0.2
------------------------------------------------------
Fixes
- Fix another mixin crash
------------------------------------------------------
Numismatics 1.0.1
------------------------------------------------------
Fixes
- Fix crash from different mixin signature on forge
------------------------------------------------------
Numismatics 1.0.0
------------------------------------------------------
Additions
- Initial Release
------------------------------------------------------
Changelog Entry Template
------------------------------------------------------
Additions
- Entry

Fixes
- Entry

Removals
- Entry