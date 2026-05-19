# Design.md — Dunelm (Dunelm.co.uk)

---

## 1. Overview

### Product Description
**Dunelm** is a UK-based fashion and homeware e-commerce retailer operating at Dunelm.co.uk. The platform serves online shoppers across desktop and responsive web, offering a broad catalogue of clothing, footwear, accessories, and home products. The core value proposition is a premium, editorially styled shopping experience that pairs strong product merchandising with fast, reliable fulfilment.

**Source Transparency:**
- Colors, typography, logo rules, and design aesthetic: fully extracted from provided brand input.
- Spacing, border, shadow, and component specs: derived from brand context and design system conventions; token values are populated from provided brand specifications where available, and follow established e-commerce design system patterns where not explicitly stated.
- Sections 9 (Component Inventory), 10 (Interaction Patterns), and 11 (Navigation Patterns): included based on defaults (Yes).
- No Figma file was provided. Token values marked with `[—]` require a design decision to be specified by the team.

### Design Fidelity
**High-fidelity (full color).** All color values are used exactly as provided in the brand guidelines. No grayscale conversion applied.

### Persona Summary
- **Primary User:** Online Shopper — browses and purchases fashion and home products via Dunelm.co.uk on desktop and mobile web.
- **Goals:** Find relevant products quickly, assess quality through imagery and editorial context, complete checkout with confidence, and manage orders post-purchase.
- **Experience Level:** Mixed — casual browsers to loyal repeat customers; UI must serve both efficiently.
- **Key Behaviours:** Responds to promotional urgency (sale badges, banners), values clean visual hierarchy, expects fast page performance and trustworthy product presentation.
- **Context:** Shops on desktop from home; also accesses on mobile during commute or leisure browsing.

### Platform
**Web — Desktop (primary) + Responsive (mobile/tablet).**

---

## 2. Colors

### Primary Palette

| Token | Value | Usage |
|---|---|---|
| `color-primary-black` | `#000000` | Page backgrounds, logo, headers, dominant UI surfaces, navbar fill |
| `color-primary-white` | `#FFFFFF` | Page backgrounds, inverse text, negative space, card surfaces |

### Neutral Palette

| Token | Value | Usage |
|---|---|---|
| `color-neutral-0` | `#FFFFFF` | Pure white — light-mode page background |
| `color-neutral-50` | `#F9F9F9` | Subtle section backgrounds, zebra rows |
| `color-neutral-100` | `#F2F2F2` | Input backgrounds, disabled fills |
| `color-neutral-200` | `#E5E5E5` | Dividers, card borders, skeleton loading |
| `color-neutral-300` | `#CCCCCC` | Placeholder text, decorative separators |
| `color-neutral-400` | `#999999` | Muted labels, secondary metadata |
| `color-neutral-500` | `#777777` | Supporting body text |
| `color-neutral-600` | `#555555` | Secondary text, captions |
| `color-neutral-700` | `#333333` | Primary body text on white |
| `color-neutral-900` | `#000000` | Maximum contrast text, overlays |

### Accent Colors

| Token | Value | Usage |
|---|---|---|
| `color-accent-teal` | `#007A7A` | Interactive elements, CTAs, links, active indicators, highlights |
| `color-accent-orange` | `#FF6A3B` | Promotional banners, sale badges, urgency-driven UI elements |

### Semantic Colors

| Token | Value | Usage |
|---|---|---|
| `color-success` | `#1A7A4A` | Order confirmation, stock confirmation, positive feedback |
| `color-warning` | `#C47A00` | Low-stock alerts, caution states |
| `color-error` | `#CC2200` | Form validation errors, destructive action feedback |
| `color-info` | `#007A7A` | Maps to `color-accent-teal`; informational callouts |

### Usage Rules
- `color-primary-black` and `color-primary-white` form the foundational surface pair — never swap for neutral mid-tones on primary surfaces.
- `color-accent-teal` is the sole interactive color; apply it to all links, primary buttons, active nav indicators, and focus rings.
- `color-accent-orange` is reserved strictly for promotional and urgency contexts (sale labels, countdown banners, special event callouts) — never use it for standard navigation or body text.
- Never use semantic colors decoratively; reserve them exclusively for system feedback (success, warning, error, info).
- All text-on-background pairings must meet WCAG AA (4.5:1 for body text, 3:1 for large text/UI components).

---

## 3. Typography

### Font Families

| Role | Family | Weights Available | Fallback Stack | Usage Context |
|---|---|---|---|---|
| Display / UI | Dunelm Display Sans | 6 weights (Thin, Light, Regular, Medium, SemiBold, Bold) | `'Helvetica Neue', Helvetica, Arial, sans-serif` | All headings, UI labels, navigation, buttons |
| Display Extended | Dunelm Display Extended | 6 weights | `'Helvetica Neue', Helvetica, Arial, sans-serif` | Hero titles and large header lockups only |
| Serif / Editorial | Dunelm Serif | Bold, Medium, Regular, Book, Light | `Georgia, 'Times New Roman', serif` | Editorial content, product descriptions, body copy |

> **Note:** Dunelm Display Extended must not be used below H1-equivalent scale. Restrict to full-width hero panels and campaign header lockups only.

### Typographic Scale (Ratio 1.4 — 140% increments)

| Token | Size | Line Height Token | Primary Weight | Usage |
|---|---|---|---|---|
| `text-xs` | 11px | `leading-tight` | Regular-400 | Legal copy, fine print, image credits |
| `text-sm` | 13px | `leading-normal` | Regular-400 / Medium-500 | Captions, metadata, product sub-labels |
| `text-md` | 16px | `leading-normal` | Regular-400 | Body copy, product descriptions, form inputs |
| `text-lg` | 18px | `leading-normal` | Medium-500 | Lead paragraphs, card teasers |
| `text-xl` | 22px | `leading-tight` | SemiBold-600 | H3 headings, section titles |
| `text-2xl` | 31px | `leading-tight` | SemiBold-600 | H2 headings, category titles |
| `text-3xl` | 43px | `leading-tight` | Bold-700 | H1 headings, page titles |
| `text-4xl` | 60px | `leading-tight` | Bold-700 | Hero titles (Dunelm Display Extended only) |
| `text-5xl` | 84px | `leading-tight` | Bold-700 | Campaign lockups (Dunelm Display Extended only) |

> Scale derived from 1.4× ratio anchored at 16px (text-md). Intermediate steps rounded to nearest whole pixel.

### Semantic Roles

| Role | Token | Weight | Family | Usage |
|---|---|---|---|---|
| Hero Title | `text-5xl` / `text-4xl` | Bold-700 | Dunelm Display Extended | Campaign heroes, seasonal landing pages |
| H1 — Page Title | `text-3xl` | Bold-700 | Dunelm Display Sans | Primary page headings |
| H2 — Section Title | `text-2xl` | SemiBold-600 | Dunelm Display Sans | Category headers, content section breaks |
| H3 — Sub-section | `text-xl` | SemiBold-600 | Dunelm Display Sans | Card headings, accordion labels, sub-sections |
| Body — Default | `text-md` | Regular-400 | Dunelm Serif | Product descriptions, editorial body copy |
| Body — Lead | `text-lg` | Medium-500 | Dunelm Serif | Introductory paragraphs, featured editorial |
| Label | `text-sm` | Medium-500 | Dunelm Display Sans | Form labels, nav items, UI controls |
| Caption | `text-sm` | Regular-400 | Dunelm Display Sans | Image captions, product metadata, timestamps |
| Fine Print | `text-xs` | Regular-400 | Dunelm Display Sans | T&Cs, footnotes, legal |
| Price — Primary | `text-xl` | Bold-700 | Dunelm Display Sans | Displayed sale/current price |
| Price — Original | `text-md` | Regular-400 | Dunelm Display Sans | Struck-through original price |
| Button Label | `text-sm` | SemiBold-600 | Dunelm Display Sans | All button text |
| Nav Item | `text-sm` | Medium-500 | Dunelm Display Sans | Global navigation links |

### Line Height

| Token | Value | Use Case |
|---|---|---|
| `leading-tight` | 1.2 | Headings, hero text, compact UI labels |
| `leading-normal` | 1.5 | Body text, default reading contexts |
| `leading-relaxed` | 1.75 | Long-form editorial, open layout articles |

### Letter Spacing

| Token | Value | Use Case |
|---|---|---|
| `tracking-tight` | `-0.02em` | Large display sizes (text-4xl, text-5xl) |
| `tracking-normal` | `0` | Body, label, caption |
| `tracking-wide` | `0.08em` | All-caps UI labels, small category tags |

### Usage Rules
- Never skip heading levels in DOM structure; always progress H1 → H2 → H3 for semantic correctness.
- Dunelm Display Extended is exclusively for hero contexts — never apply it at text-xl or below.
- Body copy in editorial/PDP contexts uses Dunelm Serif (Regular-400); UI labels and navigation always use Dunelm Display Sans.
- Minimum legible size on mobile is `text-sm` (13px) — never smaller for interactive or informational content.
- All-caps labels (e.g., category tags, nav cues) apply `tracking-wide` to preserve legibility.

---

## 4. Spacing

### Base Scale

| Token | Value |
|---|---|
| `space-0` | 0px |
| `space-1` | 4px |
| `space-2` | 8px |
| `space-3` | 12px |
| `space-4` | 16px |
| `space-5` | 20px |
| `space-6` | 24px |
| `space-8` | 32px |
| `space-10` | 40px |
| `space-12` | 48px |
| `space-16` | 64px |
| `space-20` | 80px |
| `space-24` | 96px |

### Semantic Tokens

| Token | Maps To | Usage |
|---|---|---|
| `spacing-inline-xs` | `space-1` | Icon-to-icon gaps, badge padding |
| `spacing-inline-sm` | `space-2` | Icon-to-label gap inside buttons |
| `spacing-inline-md` | `space-3` | Default horizontal gap between sibling UI elements |
| `spacing-stack-xs` | `space-1` | Tight label/value stacks (e.g., price + label) |
| `spacing-stack-sm` | `space-2` | Caption below image, metadata rows |
| `spacing-stack-md` | `space-4` | Default vertical component stacking |
| `spacing-stack-lg` | `space-6` | Card-to-card gap in grids |
| `spacing-inset-sm` | `space-2` | Compact padding — chips, badges, tight controls |
| `spacing-inset-md` | `space-4` | Default component padding — buttons, inputs |
| `spacing-inset-lg` | `space-6` | Card and panel padding |
| `spacing-inset-xl` | `space-8` | Large container padding |
| `spacing-section` | `space-16` | Between major page sections |
| `spacing-section-xl` | `space-20` | Between hero and first content section |
| `spacing-page-gutter` | `space-6` | Page-edge horizontal padding on desktop |
| `spacing-page-gutter-mobile` | `space-4` | Page-edge horizontal padding on mobile |

### Usage Rules
- All spacing must reference named tokens — no hard-coded pixel values in components or layouts.
- Use `spacing-inline` tokens for horizontal gaps between sibling elements, `spacing-stack` for vertical, `spacing-inset` for internal padding.
- Generous white space is a brand requirement — default to the Dunelm spacing step up when in doubt between two tokens.
- Section spacing (`spacing-section`) separates distinct content areas; never collapse it on desktop.

---

## 5. Border

### Base Width Tokens

| Token | Value |
|---|---|
| `border-width-0` | 0px |
| `border-width-1` | 1px |
| `border-width-2` | 2px |
| `border-width-4` | 4px |

### Border Color Tokens

| Token | Value | Usage |
|---|---|---|
| `border-color-default` | `color-neutral-200` | Default card outlines, dividers, input borders |
| `border-color-strong` | `color-neutral-400` | Emphasized boundaries, hover input states |
| `border-color-focus` | `color-accent-teal` | Focus rings on all interactive elements |
| `border-color-error` | `color-error` | Input error states |
| `border-color-disabled` | `color-neutral-100` | Disabled inputs and controls |
| `border-color-inverse` | `color-primary-white` | Borders on black/dark surfaces |
| `border-color-sale` | `color-accent-orange` | Sale price indicators, promotional card borders |

### Semantic Tokens

| Token | Width | Color | Usage |
|---|---|---|---|
| `border-input` | `border-width-1` | `border-color-default` | All text inputs, selects, textareas |
| `border-input-hover` | `border-width-1` | `border-color-strong` | Input hover state |
| `border-card` | `border-width-1` | `border-color-default` | Product cards, content cards |
| `border-divider` | `border-width-1` | `border-color-default` | Horizontal rules, section separators |
| `border-focus-ring` | `border-width-2` | `border-color-focus` | Keyboard focus indicator on any interactive element |
| `border-active-indicator` | `border-width-2` | `color-accent-teal` | Active tab underline, selected nav item |
| `border-promo` | `border-width-2` | `border-color-sale` | Promotional product card highlight border |

### Usage Rules
- Focus rings must always use `border-focus-ring` (2px teal, 2px offset) — never suppress `outline` without providing a visible alternative.
- Error state on inputs: switch to `border-color-error` only; do not increase border width.
- Use `border-divider` for content separation; never use `margin` hacks or `box-shadow` as visual dividers.
- On black surfaces, use `border-color-inverse` to maintain sufficient contrast for borders.

---

## 6. Border Radius

### Base Tokens

| Token | Value |
|---|---|
| `radius-none` | 0px |
| `radius-sm` | 2px |
| `radius-md` | 4px |
| `radius-lg` | 8px |
| `radius-xl` | 12px |
| `radius-full` | 9999px |

> **Brand note:** Dunelm maintains a predominantly sharp, editorial aesthetic. Base radii are intentionally small — the brand does not use heavily rounded UI.

### Semantic Tokens

| Token | Maps To | Usage |
|---|---|---|
| `radius-button` | `radius-md` | All buttons (primary, secondary, ghost) |
| `radius-input` | `radius-md` | Text inputs, selects, textareas |
| `radius-card` | `radius-sm` | Product cards, editorial cards — near-square is brand-correct |
| `radius-badge` | `radius-full` | Sale badges, tags, status pills |
| `radius-avatar` | `radius-full` | Account avatars |
| `radius-tooltip` | `radius-sm` | Tooltips, popovers |
| `radius-modal` | `radius-lg` | Modals, drawers, overlays |
| `radius-chip` | `radius-full` | Filter chips, size selectors |

### Usage Rules
- Product and editorial cards use `radius-card` (2px) — the near-square feel is intentional to the brand's editorial language.
- Pills, badges, and chips use `radius-full` exclusively — do not apply full-round to cards or containers.
- Nested elements reduce radius: a modal at `radius-lg` (8px) contains buttons at `radius-button` (4px).

---

## 7. Shadows / Elevation

### Base Tokens

| Token | Value | Elevation |
|---|---|---|
| `shadow-none` | `none` | 0 |
| `shadow-xs` | `0 1px 2px rgba(0,0,0,0.05)` | 1 |
| `shadow-sm` | `0 2px 4px rgba(0,0,0,0.08)` | 2 |
| `shadow-md` | `0 4px 12px rgba(0,0,0,0.10)` | 3 |
| `shadow-lg` | `0 8px 24px rgba(0,0,0,0.14)` | 4 |
| `shadow-xl` | `0 16px 40px rgba(0,0,0,0.18)` | 5 |

### Semantic Tokens

| Token | Maps To | Usage |
|---|---|---|
| `shadow-card` | `shadow-xs` | Product card resting state — minimal lift, editorial flatness |
| `shadow-card-hover` | `shadow-sm` | Product card on hover |
| `shadow-dropdown` | `shadow-lg` | Mega-nav dropdowns, select menus, filter panels |
| `shadow-modal` | `shadow-xl` | Modals, drawers, quick-view overlays |
| `shadow-input-focus` | `shadow-xs` | Subtle glow on focused inputs |
| `shadow-sticky-header` | `shadow-sm` | Sticky navigation bar on scroll |

### Usage Rules
- Dunelm's editorial aesthetic favors minimal shadow — use `shadow-card` (shadow-xs) not `shadow-md` for resting product cards.
- Elevation signals interactivity: only increase shadow on hover/active states, not on static containers.
- Modals pair `shadow-modal` with a black backdrop scrim at 50% opacity (`color-primary-black` at 0.5).
- Never combine a visible border and a heavy shadow on the same element — choose one depth signal.

---

## 8. Layout System

### Grid

| Property | Desktop (xl+) | Tablet (md–lg) | Mobile (xs–sm) |
|---|---|---|---|
| Type | 12-column | 12-column | 4-column |
| Gutter | `space-6` (24px) | `space-4` (16px) | `space-4` (16px) |
| Margin | `space-6` (24px) | `space-4` (16px) | `space-4` (16px) |
| Max content width | 1440px | 100% | 100% |

### Breakpoints

| Token | Min Width | Target Device |
|---|---|---|
| `xs` | 0px | Mobile portrait |
| `sm` | 480px | Mobile landscape |
| `md` | 768px | Tablet portrait |
| `lg` | 1024px | Tablet landscape / small desktop |
| `xl` | 1280px | Desktop |
| `2xl` | 1440px | Large desktop / wide |

### Layout Patterns

- **Full-bleed hero (single column, edge-to-edge)** — used for homepage hero banners, seasonal campaign landing pages, and promotional top-of-funnel pages.
- **Editorial two-column (content + supplementary)** — used for lookbook pages, editorial features, and brand story content.
- **Product listing grid (3-column desktop / 2-column tablet / 1-column mobile)** — used for all category, search results, and filtered listing pages (PLP).
- **Product detail centred layout (content-focused, capped width 900px)** — used for product detail pages (PDP) with full-bleed imagery and a persistent sidebar for add-to-bag actions on desktop.
- **Checkout single-column (centered, max-width 640px)** — used for all checkout steps, login, registration, and order confirmation.
- **Fixed header + fluid content shell** — persistent across all pages; sticky navigation bar at top with the page content flowing beneath.
- **Account management two-column (sidebar nav + fluid content)** — used for My Account, Order History, Saved Items, and Address Book.

### Usage Rules
- Mobile-first: write CSS from `xs` up using `min-width` breakpoints.
- Content never exceeds `1440px` max-width; always center with auto horizontal margins.
- On mobile, sidebars collapse to a bottom sheet drawer or accordion; never persist beside content.
- Product listing grids must never fall below 2 columns on mobile to preserve browsing density.
- Hero modules are full-bleed (edge-to-edge) and exempt from the standard page gutter.

---

## 9. Component Inventory

> No Figma source provided. Component inventory is derived from Dunelm brand context and e-commerce UI conventions. All component UI specs use the design tokens defined in Sections 2–7. Token values not determinable from provided input are left blank `[—]`.

---

### Button

**Anatomy:** `[icon-left]` `label` `[icon-right]`

**Variants:**

| Variant | Description |
|---|---|
| `primary` | Black fill — primary purchase actions (Add to Bag, Continue) |
| `secondary` | White fill with black border — secondary actions (Save to Wishlist, View More) |
| `ghost` | No fill, teal text — tertiary or inline actions (Edit, Remove) |
| `promo` | Orange fill — promotional urgency actions (Shop Sale, Claim Offer) |
| `danger` | Error-red fill — destructive confirmations only |

**States:** `default` · `hover` · `active` · `focus` · `disabled` · `loading`

**Sizes:**

| Size | Height | Padding H | Font Token |
|---|---|---|---|
| `sm` | 32px | `space-3` (12px) | `text-xs` |
| `md` | 44px | `space-5` (20px) | `text-sm` |
| `lg` | 52px | `space-6` (24px) | `text-md` |

**Properties:** `size` (sm/md/lg), `fullWidth` (boolean), `iconLeft` (boolean), `iconRight` (boolean), `loading` (boolean)

**Component UI Specs — Primary (md):**
```
Background:    color-primary-black (default) | color-neutral-700 (hover) | color-neutral-900 (active) | color-neutral-100 (disabled)
Text:          color-primary-white | text-sm | SemiBold-600 | leading-tight | letter-spacing: tracking-normal | text-transform: uppercase
Border:        border-width-0
Border Radius: radius-button (radius-md / 4px)
Padding:       spacing-inset-sm (8px) top/bottom, spacing-inset-md + space-1 (20px) left/right
Gap:           spacing-inline-sm (8px) between icon and label
Icons:         16px | color-primary-white | left or right
Shadow:        shadow-none (default) | shadow-xs (focus)
Height:        44px (md)
Cursor:        pointer (default) | not-allowed (disabled)
Disabled opacity: 0.4
Loading:       Spinner (16px, white) replaces label; button width locked
```

**Component UI Specs — Secondary (md):**
```
Background:    color-primary-white (default) | color-neutral-50 (hover) | color-neutral-100 (active) | color-neutral-100 (disabled)
Text:          color-primary-black | text-sm | SemiBold-600 | leading-tight | uppercase
Border:        border-width-1 | color-primary-black | solid
Border Radius: radius-button (4px)
Padding:       spacing-inset-sm top/bottom | spacing-inset-md + space-1 left/right
Shadow:        shadow-none | shadow-xs (focus)
Height:        44px
Cursor:        pointer | not-allowed (disabled)
```

**Component UI Specs — Ghost (md):**
```
Background:    transparent (all states) | color-neutral-50 (hover)
Text:          color-accent-teal | text-sm | Medium-500 | leading-tight
Border:        border-width-0
Underline:     underline on hover
Cursor:        pointer | not-allowed (disabled)
```

**Usage:**
- Use `primary` for the single most important action per view (e.g., Add to Bag). Only one primary button per screen section.
- Use `secondary` for supporting actions that do not drive the core conversion flow.
- Use `promo` exclusively in promotional banner or sale contexts — never on standard PDP/PLP pages.
- Use `danger` only in destructive-action confirmation dialogs.

---

### Input / Text Field

**Anatomy:** `label` · `input-container` (`[prefix-icon]` · `input-value` · `[suffix-icon]` · `[clear-button]`) · `[helper-text]` · `[error-message]`

**Variants:** `default` · `with-label` · `search` · `password`

**States:** `empty` · `focused` · `filled` · `error` · `disabled` · `read-only`

**Component UI Specs:**
```
Background:    color-primary-white (default) | color-neutral-50 (disabled)
Text (value):  color-primary-black | text-md | Regular-400 | leading-normal
Text (placeholder): color-neutral-400 | text-md | Regular-400
Label text:    color-primary-black | text-sm | Medium-500 | Dunelm Display Sans
Helper text:   color-neutral-500 | text-sm | Regular-400
Error text:    color-error | text-sm | Regular-400
Border:        border-width-1 | border-color-default | radius-input (4px)
Border (hover): border-width-1 | border-color-strong
Border (focus): border-width-1 | border-color-focus (color-accent-teal)
Border (error): border-width-1 | border-color-error
Padding:       space-3 (12px) top/bottom | space-4 (16px) left/right
Height:        48px (default)
Icons:         16px | color-neutral-400 (default) | color-accent-teal (focus)
Shadow:        shadow-input-focus on focus
Cursor:        text (default) | not-allowed (disabled)
Disabled opacity: 0.5
```

**Usage:**
- Label must always appear above the input — never as placeholder-only (accessibility requirement).
- Validate on blur for individual fields; validate on submit for the full form.
- Inline error messages appear directly below the field using `color-error` + `text-sm`.

---

### Product Card

**Anatomy:** `image-container` · `[sale-badge]` · `[wishlist-button]` · `product-info` (`brand-label` · `product-name` · `colour-options` · `price-row` (`current-price` · `[original-price]` · `[sale-tag]`)) · `[quick-add-button]`

**Variants:** `standard` · `editorial` · `sale` · `new-in`

**States:** `default` · `hover` · `image-hover` (swap to alt image) · `out-of-stock`

**Component UI Specs:**
```
Background (card):     color-primary-white
Image container:       aspect-ratio 3:4 | overflow: hidden | radius-card (2px)
Image hover:           scale 1.03 over 300ms ease | swap to second product image
Sale badge:            Background: color-accent-orange | Text: color-primary-white | text-xs | SemiBold-600 | tracking-wide | uppercase | radius-full | padding: space-1 space-2
Wishlist button:       Position: absolute top-right | 36×36px | Background: color-primary-white at 90% opacity | Icon: 16px heart | radius-full
Brand label:           color-neutral-500 | text-xs | Regular-400 | Dunelm Display Sans | uppercase | tracking-wide
Product name:          color-primary-black | text-sm | Regular-400 | Dunelm Display Sans | line-clamp: 2
Colour swatches:       8px dots | border: 1px color-neutral-200 | selected: border: 1.5px color-primary-black
Current price:         color-primary-black | text-sm | Bold-700 | Dunelm Display Sans
Sale price:            color-accent-orange | text-sm | Bold-700
Original price:        color-neutral-400 | text-xs | Regular-400 | text-decoration: line-through
Card border:           border-width-1 | border-color-default (hover only, not default)
Shadow:                shadow-card (default) | shadow-card-hover (hover)
Spacing (internal):    spacing-inset-sm (8px) all sides for product-info
Spacing (stack):       spacing-stack-xs (4px) between name/price lines
Cursor:                pointer
Out-of-stock overlay:  color-primary-white at 60% opacity | "Out of Stock" label: color-neutral-500 | text-sm
```

**Usage:**
- Use `sale` variant (orange price + promo badge) only when a promotional price is active — never for cosmetic purposes.
- Editorial variant uses a wider image ratio (16:9) and larger typography — restrict to editorial homepage modules or lookbook grids.

---

### Navigation Bar (Global Header)

**Anatomy:** `logo` · `primary-nav` · `[search-bar]` · `utility-nav` (`search-icon` · `account-icon` · `wishlist-icon` · `bag-icon` + `bag-count-badge`)

**States:** `default` (transparent or white on scroll) · `sticky` (black background on scroll past hero) · `mega-nav-open`

**Component UI Specs:**
```
Background (default):  color-primary-white
Background (sticky):   color-primary-black
Height:                64px desktop | 56px mobile
Logo:                  White on black surface | Black on white surface | Never distorted
Primary nav links:     color-primary-black (default) | color-primary-white (sticky) | text-sm | Medium-500 | Dunelm Display Sans | uppercase | tracking-wide
Nav link hover:        Underline (2px, color-accent-teal) | transition 150ms
Nav link active:       border-active-indicator (2px teal underline)
Utility icons:         20px | color-primary-black (default) | color-primary-white (sticky)
Bag badge:             radius-full | Background: color-accent-teal | Text: color-primary-white | text-xs | Bold-700 | min-width 18px | height 18px
Search input (expanded): border-bottom-only: 1px color-primary-black | No radius | width: 240px
Shadow (sticky):       shadow-sticky-header
Z-index:               1000
```

**Usage:**
- The logo must always follow the logo rules: white on black, black on white — no exceptions.
- Sticky state switches the entire header to black — all icons and text invert to white.
- Global search expands inline within the utility nav; it does not push page content.

---

### Sale / Promo Banner

**Anatomy:** `[countdown-timer]` · `headline` · `[sub-headline]` · `[cta-button]`

**Variants:** `full-bleed` · `inline-strip` · `category-header`

**Component UI Specs:**
```
Background:     color-accent-orange (primary promotional) | color-primary-black (secondary promo)
Text (headline): color-primary-white | text-2xl – text-3xl | Bold-700 | Dunelm Display Sans | uppercase
Text (sub):     color-primary-white at 85% opacity | text-md | Regular-400 | Dunelm Serif
CTA button:     White fill | Black text | radius-button | height 44px
Countdown:      color-primary-white | text-lg | Bold-700 | Mono-spaced tabular figures
Padding:        spacing-inset-xl (32px) vertical | full-bleed: edge-to-edge horizontal
```

**Usage:**
- Promotional banners use `color-accent-orange` exclusively — never teal or neutral fills for urgency banners.
- Full-bleed banners sit above the fold on sale campaign pages; inline-strip banners appear between content sections.

---

### Filter Panel

**Anatomy:** `panel-header` · `filter-groups` (`filter-label` · `filter-options`) · `apply-button` · `clear-all-link`

**Variants:** `sidebar` (desktop) · `bottom-sheet` (mobile)

**States:** `collapsed` · `expanded` · `option-selected` · `loading`

**Component UI Specs:**
```
Background:         color-primary-white
Panel border:       border-right: border-width-1 | border-color-default (sidebar only)
Group label:        color-primary-black | text-sm | SemiBold-600 | Dunelm Display Sans | uppercase | tracking-wide
Filter option text: color-neutral-700 | text-sm | Regular-400 | Dunelm Display Sans
Selected option:    color-primary-black | text-sm | Medium-500 | Checkbox: filled black
Checkbox (selected): 16px | Background: color-primary-black | Icon: checkmark white
Chip (applied filter): Background: color-primary-black | Text: color-primary-white | text-xs | radius-full | removable ×
Apply button:       Full-width | Primary variant | height 44px
Clear all:          Ghost variant | color-accent-teal | text-sm
```

---

### Toast / Notification

**Anatomy:** `[icon]` · `message` · `[action-link]` · `dismiss-button`

**Variants:** `success` · `warning` · `error` · `info`

**Component UI Specs:**
```
Background:     success: color-success | warning: color-warning | error: color-error | info: color-neutral-900
Text:           color-primary-white | text-sm | Regular-400
Icon:           16px | color-primary-white
Dismiss:        16px × icon | color-primary-white at 70% opacity | hover: 100%
Border Radius:  radius-lg (8px)
Padding:        spacing-inset-md (16px)
Max-width:      400px
Position:       Fixed bottom-right (desktop) | Fixed bottom-center (mobile)
Shadow:         shadow-lg
Z-index:        9000
Animation:      Slide-up + fade-in 200ms ease | Auto-dismiss: success/info 4s | warning/error: persistent
```

---

### Badge / Tag

**Anatomy:** `[icon]` · `label`

**Variants:** `sale` · `new-in` · `low-stock` · `exclusive` · `out-of-stock`

**Component UI Specs:**
```
sale:        Background: color-accent-orange | Text: color-primary-white | text-xs | SemiBold-600 | uppercase | tracking-wide | radius-full | padding: space-1 space-2
new-in:      Background: color-primary-black | Text: color-primary-white | same type rules
low-stock:   Background: color-warning | Text: color-primary-white
exclusive:   Background: color-primary-black | Text: color-primary-white | Border: 1px color-neutral-400
out-of-stock: Background: color-neutral-200 | Text: color-neutral-500
```

---

### Breadcrumb

**Anatomy:** `home-link` · `separator` · `[intermediate-links]` · `current-page`

**Component UI Specs:**
```
Link text:      color-accent-teal | text-sm | Regular-400 | Dunelm Display Sans | underline on hover
Current page:   color-neutral-500 | text-sm | Regular-400 | no link
Separator:      "/" character | color-neutral-300 | spacing: space-2 each side
```

---

### Accordion

**Anatomy:** `trigger` (`label` · `icon-chevron`) · `panel-content`

**States:** `collapsed` · `expanded`

**Component UI Specs:**
```
Trigger background:   color-primary-white (default) | color-neutral-50 (hover)
Trigger text:         color-primary-black | text-sm | Medium-500 | Dunelm Display Sans
Chevron icon:         16px | color-primary-black | rotate 180° when expanded | transition 200ms
Border:               border-bottom: border-width-1 | border-color-default
Padding:              space-4 (16px) vertical | space-0 horizontal
Panel padding:        space-4 (16px) bottom | space-0 top
Animation:            max-height expand 200ms ease-out
```

---

### Pagination

**Anatomy:** `prev-button` · `page-numbers` · `Dunelm-button`

**Component UI Specs:**
```
Page number (default):  color-neutral-500 | text-sm | Regular-400 | 36×36px touch target | radius-md
Page number (active):   Background: color-primary-black | Text: color-primary-white | Bold-700 | radius-md
Page number (hover):    Background: color-neutral-100
Prev/Dunelm buttons:      Arrow icon 16px | color-primary-black | disabled: color-neutral-300
Gap:                    spacing-inline-xs (4px) between items
```

---

### Size Selector

**Anatomy:** `size-option` (one per available size) · `[size-guide-link]`

**States:** `available` · `selected` · `out-of-stock`

**Component UI Specs:**
```
Available:        Border: 1px color-neutral-300 | Background: color-primary-white | Text: color-primary-black | text-sm | min-width 44px | height 44px | radius-chip (full) | cursor: pointer
Selected:         Border: 2px color-primary-black | Background: color-primary-black | Text: color-primary-white
Out-of-stock:     Border: 1px color-neutral-200 | Background: color-neutral-50 | Text: color-neutral-300 | text-decoration: line-through | cursor: not-allowed
Hover:            Border: 1px color-primary-black
```

---

## 10. Interaction Patterns

### Interaction Principles
1. Every user action must produce visible feedback within 100ms — no silent clicks.
2. The editorial aesthetic extends to motion — transitions are deliberate and unhurried (150–300ms), never jarring or distracting.
3. Errors are surfaced at the earliest possible moment (on blur for forms) and resolved at point-of-occurrence — never batched at submit-time only.
4. Promotional urgency (countdowns, low-stock) must be honest — never manufactured scarcity.
5. Mobile touch targets are never smaller than 44×44px, regardless of visual design context.

### Global Interaction Rules
- Click/tap targets: minimum 44×44px for all interactive elements.
- Debounce rapid-fire search queries by 300ms after last keystroke.
- All form submissions debounced 300ms to prevent duplicate orders.
- Transitions: fade-in/slide-up at 150–200ms ease; page-level transitions at 200ms ease-out.

### Form Interactions
- Validate on `blur` for individual fields; validate on `submit` for the full form.
- Inline error messages appear directly below the triggering field using `color-error` + `text-sm`.
- Required fields are marked with an asterisk (*) adjacent to the label, never as placeholder text.
- Autofill support for address, email, and payment fields — never block browser autofill.

### Validation & Error Handling
- Error messages follow the pattern: "[Field name] [what went wrong]." — e.g., "Email address must include an @ symbol."
- Required field errors appear only after the user has interacted with the field (not on page load).
- Never clear user-entered data on validation fail — preserve the value, highlight the error.

### Submission & Feedback States
- Add-to-bag and checkout submit buttons enter a `loading` state on click: spinner replaces label, button is disabled.
- On success: inline toast (auto-dismiss 4s) for non-critical actions; full-page redirect for checkout completion.
- On failure: persistent inline error banner directly above the form action area with a retry CTA.

### Loading States
- **Product listing (PLP):** Skeleton card grid matching the expected layout — avoids layout shift.
- **PDP images:** Progressive blur-up image load.
- **Search results:** Skeleton rows for instant perceived response.
- **Checkout steps:** Inline spinner within the submit button; page does not re-render.
- **Spinner overlays:** Only for actions taking > 1 second; never on fast operations.

### Empty States
Every list, search result, or filtered view must have an empty state containing:
- An editorial illustration or minimal icon.
- A clear headline ("No results for '[search term]'").
- A helpful description and a primary action (e.g., "Clear filters" or "Browse New In").

### Error States (System-Level)
- **404:** Brand-consistent message, search bar, links to homepage and popular categories.
- **500:** Apologetic tone, auto-retry option, customer service link — no technical error codes shown.
- **Out of stock (PDP):** Size grayed out, "Notify me when back in stock" CTA replaces Add to Bag.
- **Offline:** Persistent top banner; disable purchase actions; show cached browse data where possible.

### Feedback Patterns

| Type | Component | Duration | Dismissible |
|---|---|---|---|
| Success | Toast (bottom-right desktop / bottom-center mobile) | 4s auto | Yes |
| Warning | Inline banner | Persistent | Yes |
| Info | Toast | 6s auto | Yes |
| Error | Inline banner (above form / action) | Persistent | Yes (with action) |

### Confirmation Flows
- Destructive actions (remove from bag, delete saved card, cancel order) require a confirmation dialog.
- Dialog explicitly states the consequence: "Remove [Product Name] from your bag?"
- Confirm button uses `danger` variant; cancel uses `secondary`.

### Search & Filtering
- Search debounce: 300ms after last keystroke before triggering request.
- Active filters displayed as removable black chips above the product grid.
- "Clear all filters" always visible when one or more filters are active.
- Filter panel updates product count live as filters are toggled (before applying).

### Product Interaction Patterns
- **PLP cards:** Hover swaps to alternate product image (300ms cross-fade); wishlist heart appears on hover.
- **Size selection:** Tap/click required before Add to Bag is enabled; unselected state triggers inline prompt.
- **Image gallery (PDP):** Swipe on mobile; thumbnail click on desktop; zoom on click/pinch.
- **Colour selector:** Immediate image swap on colour change; URL updates for deep-linking.

### Hover, Focus & Active States
- **Hover:** Background tint (`color-neutral-50`) on interactive containers; underline on text links; image scale on product cards.
- **Focus:** `border-focus-ring` (2px `color-accent-teal`, 2px offset) on every focusable element — never suppress.
- **Active:** Darken one shade (e.g., black button → neutral-700) or scale 98% for 50ms.

### Unsaved Changes
- Dirty checkout form triggers "You have unsaved changes" dialog on back-navigation.
- Dialog offers: Save & Continue, Discard, Stay on Page.

### Progressive Disclosure
- PDP: product details in accordion (Delivery, Returns, Care Guide, Sustainability).
- Never hide primary purchase actions (Add to Bag, size selection) behind progressive disclosure.

---

## 11. Navigation Patterns

### Primary Navigation (Global Header)
- **Pattern:** Horizontal top bar, full-width, sticky on scroll (switches to black).
- **Max items:** 7 ± 2 top-level categories (Women, Men, Kids, Home, Beauty, Sale, Brands).
- **Hover behaviour:** Triggers mega-nav dropdown with subcategories, editorial images, and featured links.
- **Mobile:** Collapses to hamburger icon; opens a full-screen slide-in drawer.

### Mega-Nav (Desktop)
- Triggered on hover of primary nav item; dismissed on mouse-out or Escape.
- Contains: subcategory links, featured product callouts (image + title + link), editorial "spotlight" module.
- Background: `color-primary-white` | Shadow: `shadow-dropdown` | Padding: `spacing-inset-xl`.
- Max 4 subcategory columns; editorial image column always rightmost.

### Secondary Navigation
- **Pattern:** Tab bar or horizontal scroll of category chips within a section (e.g., within Women: Tops, Dresses, Trousers…).
- Visually subordinate: `text-sm`, `Regular-400`, `color-neutral-500`; active tab uses `border-active-indicator` (2px teal underline) and `color-primary-black` text.

### Contextual Navigation
- **Breadcrumbs:** Present on all PLP and PDP pages, and any page deeper than 2 hierarchy levels.
- **Sticky "Add to Bag" bar:** Appears on PDP when the main Add to Bag button scrolls out of view — contains: product name, size selector, and Add to Bag button.
- **In-page scroll nav:** On very long editorial or campaign pages only.

### Search Navigation
- **Trigger:** Search icon in utility nav (desktop expands inline; mobile opens full-screen overlay).
- **Shortcut:** Not applicable for consumer web — icon-triggered only.
- **Behaviour:** Instant suggestions (debounced 300ms): recent searches, trending queries, matched products, matched categories.
- Results grouped: "Suggestions" · "Products" · "Categories."

### Account Navigation
- **Pattern:** Two-column — persistent left sidebar (desktop) / accordion (mobile).
- **Items:** My Orders, My Dunelm, Saved Items, Address Book, Payment Methods, Account Details, Sign Out.
- **Active state:** `border-active-indicator` (2px teal left-border) + `color-primary-black` label text.

### Navigation Feedback

| State | Style |
|---|---|
| Active (primary nav) | Bold label + 2px teal underline (`border-active-indicator`) |
| Hover (primary nav) | Teal underline appears (150ms transition) |
| Focus | `border-focus-ring` (2px teal, 2px offset) |
| Active (account sidebar) | 2px teal left border + `color-primary-black` text |
| Hover (account sidebar) | `color-neutral-50` background tint |

### Navigation Rules
- Never open internal links in new browser tabs.
- Mobile primary nav must use the hamburger drawer — never a bottom tab bar (brand standard).
- Sale and promotional categories always appear in `color-accent-orange` text within the nav to signal urgency.
- Current category/page is always visually indicated — never ambiguous to the user.

---

## 12. Accessibility Standards

### Target Tier
**WCAG 2.1 Level AA** across all pages and components.

### Contrast Rules
- Normal text (< 18px regular / < 14px bold): minimum **4.5:1** contrast ratio against background.
- Large text (≥ 18px regular or ≥ 14px bold): minimum **3:1**.
- UI components and graphical objects (icons, borders, focus rings): minimum **3:1**.
- Critical pairings to test:
  - `color-accent-teal` (`#007A7A`) on `color-primary-white` — verify ≥ 4.5:1 for small text links.
  - `color-accent-orange` (`#FF6A3B`) on `color-primary-white` — verify ≥ 3:1 for badge use (large text context).
  - `color-primary-white` on `color-primary-black` — passes at maximum ratio.
  - `color-neutral-500` on `color-primary-white` — verify for caption/metadata uses.

### Keyboard Navigation
- All interactive elements reachable via `Tab` in logical DOM order (left-to-right, top-to-bottom).
- `Enter` / `Space` activates buttons and links.
- Arrow keys navigate within composite widgets (mega-nav dropdowns, size selectors, carousels, date pickers).
- `Escape` closes modals, drawers, mega-nav, and popovers.
- Skip-to-main-content link visible on first Tab keypress on every page.

### Focus States
- `border-focus-ring` (2px, `color-accent-teal`, 2px offset) applied to every focusable element.
- Never use `outline: none` without providing a visible custom focus indicator.
- Focus order matches visual reading order — never reordered by CSS positioning tricks.
- Mega-nav: focus is trapped within the open dropdown; Escape returns focus to the triggering nav item.

### Screen Reader Considerations
- All product images have descriptive `alt` text: "[Brand] [Product Name] in [Color]".
- Decorative images (backgrounds, dividers) use `alt=""` + `aria-hidden="true"`.
- Dynamic content changes (filter results updating, toast notifications) announced via `aria-live` regions: `polite` for non-urgent updates, `assertive` for errors.
- All form fields have associated `<label>` elements or `aria-label` — no placeholder-as-label.
- Modals use `role="dialog"` + `aria-modal="true"` + `aria-labelledby` pointing to the modal heading.
- Icon-only buttons (wishlist heart, bag, search) use `aria-label` describing the action (e.g., `aria-label="Add to Wishlist"`).
- Bag count badge: announced as "Shopping bag, 3 items" via `aria-label` on the bag button.

### Colour Independence
- All state information (error, success, selected, active) is communicated by both colour AND a secondary signal (icon, label text, border, pattern) — never by colour alone.
- Sale prices in orange are always paired with a visual "Was/Now" label or strikethrough — not price colour alone.
- Out-of-stock sizes use strikethrough text in addition to grayed colour.

### Motion & Animation
- Respect `prefers-reduced-motion` media query: disable or reduce all transitions and animations for users who have opted in to reduced motion at OS level.
- Essential animations (loading spinners, progress indicators) may remain at reduced motion but should not loop distractingly.

---

*Design.md — Dunelm (Dunelm.co.uk) | High-fidelity | Generated from brand guidelines and product context input | No Figma source provided — component specs derived from brand context and e-commerce conventions. All blank `[—]` values require a design team decision.*
