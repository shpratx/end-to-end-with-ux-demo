# Dunelm — UI Design System
**Dunelm Group plc · "The Home of Homes" · E-commerce & Mobile App**
Version 1.0 · June 2026

> Colour values in this document were sampled directly from the supplied app
> screenshots and marketing assets. Where exact brand fonts are proprietary, a
> close, freely-licensable substitute is named so the system is immediately
> usable on wireframes. Replace substitutes with the licensed brand faces in
> production.

---

## 1. Brand Identity

Dunelm is a **light-first, warm, friendly homeware retail brand**. The visual
language is **homely-premium**: generous white space, rounded forms, soft
shadows, large lifestyle photography, and a confident leaf-green accent. It is
the deliberate opposite of a dense industrial dashboard — the product *is* the
home, so the UI recedes and lets imagery and product lead.

Two greens carry the brand. A **deep forest green** anchors hero/marketing
surfaces and brand moments; a **bright action green** drives every interactive
element (buttons, links, active states). Everything else is warm neutral.

**Design principles**
- **Light surfaces as the default** — white and warm off-white, never dark mode.
- **One accent that means "go"** — bright green is reserved for action and
  active state; it is never decorative.
- **Rounded and soft** — pill buttons, rounded cards, gentle shadows. No hard
  industrial edges.
- **Photography leads** — large, warm, lived-in lifestyle imagery; the chrome
  stays quiet around it.
- **Breathing room over density** — generous padding; one clear primary action
  per screen.
- **Mobile-first, responsive up** — patterns are designed for the app first,
  then scale to web.

---

## 2. Colour Palette

### Core Brand
| Token | Hex | Usage |
|---|---|---|
| `--green-action` | `#0A8A00` | Primary CTA buttons, links, active nav item, app header, focus accents |
| `--green-action-hover` | `#067006` | Hover/pressed state on green buttons |
| `--green-forest` | `#023F2F` | Marketing/hero backgrounds, brand banners, app account header, splash |
| `--green-forest-deep` | `#012A20` | Darkest brand green for layering on forest surfaces |
| `--green-link` | `#0A7A0A` | Inline text links on light surfaces (slightly deeper for contrast) |
| `--on-green` | `#FFFFFF` | Text/icons placed on any green surface |

### Surface & Background
| Token | Hex | Usage |
|---|---|---|
| `--bg-page` | `#E9EAE5` | App/page background (warm light grey) |
| `--bg-surface` | `#FFFFFF` | Cards, panels, sheets, app content area, search bar |
| `--bg-surface-alt` | `#F6F6F3` | Subtle alternate fill, inactive chip, input rest |
| `--bg-hero` | `#023F2F` | Full-bleed brand hero / marketing sections |
| `--bg-row-hover` | `#F4F5F1` | List/table row hover |
| `--bg-overlay` | `rgba(2,42,32,0.55)` | Scrim behind modals & bottom sheets |

### Text
| Token | Hex | Usage |
|---|---|---|
| `--text-primary` | `#1C1C1A` | Headings & body on light surfaces |
| `--text-secondary` | `#5A5A55` | Supporting text, captions, dimensions, dates |
| `--text-dim` | `#8C8C86` | Placeholder, disabled, metadata |
| `--text-on-dark` | `#FFFFFF` | Text on forest-green / slate surfaces |
| `--text-on-dark-muted` | `#C6CEC9` | Secondary text on forest green |
| `--text-link` | `#0A7A0A` | Link text (underline on hover) |
| `--text-price` | `#1C1C1A` | Standard price |
| `--text-price-was` | `#8C8C86` | Strikethrough "Was £…" price |

### Borders & Dividers
| Token | Hex | Usage |
|---|---|---|
| `--border-light` | `#E2E2DD` | Card borders, list dividers, hairlines |
| `--border-input` | `#C9C9C3` | Form input borders (rest) |
| `--border-strong` | `#1C1C1A` | High-emphasis outline (secondary button on light) |
| `--border-focus` | `#0A8A00` | Focus ring (paired with offset) |

### Semantic / Status
| Token | Hex | Usage |
|---|---|---|
| `--sale-red` | `#D8232A` | Sale banners, sale badges, promotional flashes |
| `--sale-red-dark` | `#B41C22` | Sale-red hover |
| `--info-blue` | `#0066CC` | "New" labels, informational tags |
| `--success` | `#0A8A00` | In stock / available / confirmed (reuses action green) |
| `--warning` | `#E08600` | Low stock, attention |
| `--error` | `#C62828` | Errors, validation, unavailable |
| `--star-rating` | `#F5B400` | Review stars |

### Footer (Slate)
| Token | Hex | Usage |
|---|---|---|
| `--slate-footer` | `#2F3A40` | Global footer background |
| `--slate-footer-text` | `#FFFFFF` | Footer headings & links |
| `--slate-footer-muted` | `#AEB7BC` | Footer secondary text |

---

## 3. Typography

Dunelm pairs a **serif display face** (warm, editorial — used for headlines and
brand moments) with a **humanist sans** (clean, legible — used for all UI and
body copy). The wordmark is a bespoke rounded sans and should only ever be
rendered from the official logo asset, never re-typeset.

```css
/* Display / headings — brand serif (substitute: Lora / Fraunces) */
--font-display: 'Lora', Georgia, 'Times New Roman', serif;

/* UI & body — humanist sans (substitute: Inter / system) */
--font-ui: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
```

> **Production note:** Dunelm's licensed brand faces are proprietary. `Lora`
> (serif) and `Inter` (sans) are the closest free analogues and are what these
> tokens assume. Swap the `font-family` values when the licensed fonts are
> available — every other token stays the same.

### Scale
| Role | Font | Size (mobile / web) | Weight | Colour |
|---|---|---|---|---|
| Hero headline | display serif | 28 / 40px | 700 | `--text-on-dark` or `--text-primary` |
| Page title | display serif | 22 / 28px | 600 | `--text-primary` |
| Section heading | display serif | 18 / 22px | 600 | `--text-primary` |
| Card / product title | UI sans | 15 / 16px | 500 | `--text-primary` |
| Body | UI sans | 15 / 16px | 400 | `--text-primary` |
| Body small / supporting | UI sans | 13 / 14px | 400 | `--text-secondary` |
| Caption / metadata | UI sans | 12 / 12px | 400 | `--text-dim` |
| Price | UI sans | 16 / 18px | 700 | `--text-price` |
| Price (was) | UI sans | 12 / 13px | 400 strike | `--text-price-was` |
| Button label | UI sans | 15 / 16px | 600 | varies |
| Nav / tab label | UI sans | 11 / 13px | 500 | varies |
| Badge / chip | UI sans | 11 / 12px | 600 | varies |
| Link | UI sans | inherits | 600 | `--text-link`, underline |

Line-height: **1.25** for display/headings, **1.5** for body. Letter-spacing:
default; hero headlines may use `-0.01em`.

---

## 4. Spacing

Base unit: **4px**. All spacing is a multiple of 4. Dunelm runs slightly more
generous than a dense enterprise grid — favour the larger steps for layout.

```
4px   — xs   (icon gap, tight internal)
8px   — sm   (chip padding, badge gap)
12px  — md   (compact internal padding)
16px  — lg   (card padding, default gap)
20px  — xl   (card padding comfortable)
24px  — 2xl  (section gaps)
32px  — 3xl  (page padding / section margins)
48px  — 4xl  (major marketing section rhythm)
```

---

## 5. Radius, Elevation & Motion

Dunelm is **soft and rounded**. Buttons are full pills; cards and inputs use a
medium radius; shadows are gentle and warm-neutral.

### Radius
```css
--radius-sm:   8px;    /* inputs, small chips, image thumbs */
--radius-md:   12px;   /* cards, panels, sheets */
--radius-lg:   16px;   /* large cards, modals */
--radius-pill: 999px;  /* all buttons, search bar, filter chips, swatches */
--radius-full: 50%;    /* favourite/heart circle, avatar, icon buttons */
```

### Elevation
```css
--shadow-none:  none;
--shadow-card:  0 1px 3px rgba(20,20,18,0.06), 0 2px 8px rgba(20,20,18,0.05);
--shadow-float: 0 4px 16px rgba(20,20,18,0.10);   /* floating card over hero */
--shadow-sheet: 0 -4px 24px rgba(2,42,32,0.18);   /* bottom sheet */
--shadow-modal: 0 8px 40px rgba(2,42,32,0.25);
```

### Motion
```css
--transition-fast: 0.15s ease;   /* hover, colour, small state */
--transition-base: 0.25s ease;   /* sheets, accordions, fades */
--ease-out:        cubic-bezier(0.16, 1, 0.3, 1);  /* sheet/drawer entrances */
```
Interactions feel responsive but unhurried. Avoid bounce; use `--ease-out` for
sheet and drawer entrances.

---

## 6. Components

### 6.1 Buttons

All buttons are **full pills** (`--radius-pill`). Mobile height **52px**, web
**48px**. Generous horizontal padding.

**Primary (Green CTA)** — "Add to Basket", "Sign Up", "See what's new"
```css
.btn-primary {
  background: #0A8A00;
  color: #FFFFFF;
  font: 600 16px/1 var(--font-ui);
  height: 52px;
  padding: 0 28px;
  border: none;
  border-radius: 999px;
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  cursor: pointer;
  transition: background var(--transition-fast);
}
.btn-primary:hover  { background: #067006; }
.btn-primary:active { background: #045200; }
.btn-primary:disabled { background: #BFD9BC; color: #FFFFFF; cursor: not-allowed; }
```

**Secondary (Outline)** — "Select Options"
```css
.btn-secondary {
  background: #FFFFFF;
  color: #1C1C1A;
  font: 600 16px/1 var(--font-ui);
  height: 52px;
  padding: 0 24px;
  border: 1.5px solid #1C1C1A;
  border-radius: 999px;
  cursor: pointer;
  transition: background var(--transition-fast);
}
.btn-secondary:hover { background: #F4F5F1; }
```

**Tertiary (Add / compact icon-label)** — "Add" on product cards
```css
.btn-tertiary {
  background: #FFFFFF;
  color: #1C1C1A;
  border: 1.5px solid #C9C9C3;
  border-radius: 999px;
  height: 44px; padding: 0 18px;
  display: inline-flex; align-items: center; gap: 6px;
  font: 600 15px/1 var(--font-ui);
}
.btn-tertiary:hover { border-color: #1C1C1A; }
```

**On-forest button** (primary CTA shown on a forest-green hero, e.g. "See
what's new") — inverts to a dark pill with white text, or uses an outline:
```css
.btn-on-hero {
  background: #012A20;
  color: #FFFFFF;
  border: 1.5px solid rgba(255,255,255,0.35);
  border-radius: 999px;
  height: 52px; padding: 0 28px;
  font: 600 16px/1 var(--font-ui);
}
```

**Sale button**
```css
.btn-sale { background: #D8232A; color:#FFFFFF; /* …pill, 52px… */ }
.btn-sale:hover { background:#B41C22; }
```

### 6.2 Top Bar / Search (Mobile App)

White surface with a pill search field, mic, and basket icon. App account
screens use the **forest-green** header instead (see 6.10).

```css
.app-topbar {
  height: 56px;
  background: #FFFFFF;
  display: flex; align-items: center; gap: 12px;
  padding: 0 16px;
}
.search-pill {
  flex: 1;
  height: 44px;
  background: #FFFFFF;
  border: 1.5px solid #C9C9C3;
  border-radius: 999px;
  padding: 0 16px;
  display: flex; align-items: center; gap: 8px;
  font: 400 15px/1 var(--font-ui);
  color: #8C8C86;            /* placeholder "Search Dunelm" */
}
.search-pill:focus-within { border-color: #0A8A00; box-shadow: 0 0 0 3px rgba(10,138,0,0.15); }
.topbar-icon { width: 24px; height: 24px; color: #1C1C1A; }
```

### 6.3 Bottom Tab Navigation (Mobile)

Five line-icon tabs: **Home · Shop · Favourites · Stores · Account**. Active tab
turns green with a short green underline indicator above it.

```css
.tabbar {
  height: 64px;
  background: #FFFFFF;
  border-top: 1px solid #E2E2DD;
  display: flex; justify-content: space-around; align-items: center;
}
.tab {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  font: 500 11px/1 var(--font-ui);
  color: #5A5A55;
  position: relative;
}
.tab .tab-icon { width: 22px; height: 22px; stroke-width: 1.75; }
.tab.active { color: #0A8A00; }
.tab.active::before {            /* short top indicator bar */
  content: ""; position: absolute; top: -10px;
  width: 24px; height: 3px; border-radius: 0 0 3px 3px;
  background: #0A8A00;
}
```

### 6.4 Top Navigation (Web)

```css
.web-nav { height: 56px; background:#FFFFFF; display:flex; align-items:center; gap:24px; padding:0 32px; }
/* logo · search pill (centre, flexible) · favourites · account · basket */
.web-nav-link { font: 400 15px var(--font-ui); color:#1C1C1A; padding: 18px 0; border-bottom: 3px solid transparent; }
.web-nav-link:hover { color:#0A8A00; }
.web-nav-link.active { color:#0A8A00; border-bottom-color:#0A8A00; font-weight:600; }
.web-nav-link.sale { color:#D8232A; }     /* "Sale" entry */
.web-nav-link.feature { color:#0A8A00; }  /* "New & Inspiration" entry */
```
A thin **sale-red strip** may sit directly below the nav for promotions:
```css
.promo-strip { background:#D8232A; color:#FFFFFF; height:40px; font:600 14px var(--font-ui); text-align:center; }
.promo-strip a { color:#FFFFFF; text-decoration: underline; }
```

### 6.5 Product Card

The workhorse component: image with a favourite heart, title, variant/size,
price (with optional "Was" strike + Price History link), and an action button.

```css
.product-card {
  background: #FFFFFF;
  border-radius: 12px;
  overflow: hidden;
  display: flex; flex-direction: column;
}
.product-card .media { position: relative; aspect-ratio: 1 / 1; background:#F6F6F3; }
.product-card .fav {                 /* heart, top-right */
  position: absolute; top: 10px; right: 10px;
  width: 36px; height: 36px; border-radius: 50%;
  background: #FFFFFF; box-shadow: var(--shadow-card);
  display:flex; align-items:center; justify-content:center;
}
.product-card .fav svg { width:18px; height:18px; color:#1C1C1A; }
.product-card .fav.active svg { color:#0A8A00; fill:#0A8A00; }
.product-card .body { padding: 12px 4px 0; }
.product-card .title { font: 500 15px/1.3 var(--font-ui); color:#1C1C1A; }
.product-card .meta  { font: 400 13px var(--font-ui); color:#5A5A55; margin-top:4px; }
.product-card .price { font: 700 16px var(--font-ui); color:#1C1C1A; margin-top:6px; }
.product-card .price-was { font: 400 12px var(--font-ui); color:#8C8C86; text-decoration: line-through; }
.product-card .actions { display:flex; gap:8px; margin-top:12px; }
```

**Colour swatches row** (e.g. "8 Colours"):
```css
.swatches { display:flex; align-items:center; gap:6px; }
.swatch { width:18px; height:18px; border-radius:50%; border:1px solid #E2E2DD; }
.swatch.more { font: 600 12px var(--font-ui); color:#5A5A55; }  /* "+" overflow */
```

### 6.6 Badges & Labels

```css
.badge { display:inline-block; padding:3px 10px; border-radius:999px; font:600 11px var(--font-ui); }
.badge-new      { background:#FFFFFF; color:#0066CC; }                 /* "New" */
.badge-sale     { background:#D8232A; color:#FFFFFF; }                 /* "Sale" */
.badge-count    { background:#D8232A; color:#FFFFFF; border-radius:50%; min-width:18px; height:18px; font-size:10px; } /* basket/notify count */
.badge-instock  { background:#E7F4E6; color:#0A7A0A; border:1px solid #BFE3BC; }
.badge-low      { background:#FCF1E0; color:#A66400; border:1px solid #F0D9B0; }
.badge-oos      { background:#FBEAEA; color:#B41C22; border:1px solid #F1C7C7; }
.badge-neutral  { background:#F4F5F1; color:#5A5A55; border:1px solid #E2E2DD; }
```
The small **"New"** label observed on cards is set in `--info-blue` directly on
the white surface (no fill) at 11–12px/600.

### 6.7 Filter Chips & Filter Bar

A horizontal row of pill chips. The primary "All Filters" chip is filled green
with an icon; attribute chips ("Size", "Colour") are outlined.

```css
.filter-bar { display:flex; gap:8px; padding:12px 16px; overflow-x:auto; }
.chip-filter {
  height:40px; padding:0 16px; border-radius:999px;
  display:inline-flex; align-items:center; gap:6px;
  font:600 14px var(--font-ui); white-space:nowrap; cursor:pointer;
}
.chip-filter.outline { background:#FFFFFF; color:#1C1C1A; border:1.5px solid #C9C9C3; }
.chip-filter.outline:hover { border-color:#1C1C1A; }
.chip-filter.primary { background:#0A8A00; color:#FFFFFF; border:none; }   /* "All Filters" */
.chip-filter.selected { background:#E7F4E6; color:#0A7A0A; border:1.5px solid #0A8A00; }
```
Results/sort line: `177 results sorted by Most Relevant ⌄` — count in 600,
"Most Relevant" rendered as a green underlined link.

### 6.8 Web Filter Sidebar

```css
.filter-sidebar { width:260px; background:#FFFFFF; border:1px solid #E2E2DD; border-radius:12px; padding:20px; }
.filter-group + .filter-group { border-top:1px solid #E2E2DD; padding-top:16px; margin-top:16px; }
.filter-input {
  width:100%; height:44px; padding:0 14px;
  border:1.5px solid #C9C9C3; border-radius:8px;
  font:400 14px var(--font-ui); color:#1C1C1A; background:#FFFFFF;
}
.filter-input:focus { outline:none; border-color:#0A8A00; box-shadow:0 0 0 3px rgba(10,138,0,0.15); }
.filter-checkbox { accent-color:#0A8A00; width:18px; height:18px; }
```

### 6.9 Forms & Inputs

```css
.input {
  height:48px; padding:0 16px;
  border:1.5px solid #C9C9C3; border-radius:8px;     /* note: text inputs use radius-sm, not pill */
  font:400 15px var(--font-ui); color:#1C1C1A; background:#FFFFFF;
}
.input::placeholder { color:#8C8C86; }
.input:focus { outline:none; border-color:#0A8A00; box-shadow:0 0 0 3px rgba(10,138,0,0.15); }
.input.error { border-color:#C62828; }
.input-label { font:600 13px var(--font-ui); color:#1C1C1A; margin-bottom:6px; }
.input-error-text { font:400 12px var(--font-ui); color:#C62828; margin-top:4px; }
```
**Newsletter / inline submit** (from footer): a single pill wrapper containing a
borderless field with a green pill "Sign Up" button on the right.

**Quantity stepper** (PDP):
```css
.stepper { display:inline-flex; align-items:center; border:1.5px solid #C9C9C3; border-radius:999px; height:52px; }
.stepper button { width:44px; background:none; border:none; font-size:20px; color:#1C1C1A; }
.stepper .value { min-width:32px; text-align:center; font:600 16px var(--font-ui); }
```

### 6.10 Account Header (Forest Green)

Account/profile surfaces lead with a forest-green header carrying the user name,
message and basket icons. A white "most recent order" card floats over the seam.

```css
.account-header {
  background:#023F2F; color:#FFFFFF;
  padding:16px 20px 28px;
}
.account-header .name { font:600 20px var(--font-display); }
.account-header .icon-circle {
  width:40px; height:40px; border-radius:50%;
  background:rgba(255,255,255,0.12);
  display:flex; align-items:center; justify-content:center;
}
.floating-card {                /* "Most recent order" */
  background:#FFFFFF; border-radius:16px;
  box-shadow:var(--shadow-float);
  padding:20px; margin:-24px 16px 0;   /* pulls up over the green seam */
}
```

### 6.11 List Row (Account menu, Inbox)

```css
.list-row {
  display:flex; align-items:center; gap:14px;
  padding:18px 4px;
  border-bottom:1px solid #E2E2DD;
}
.list-row .lead-icon { width:22px; height:22px; color:#1C1C1A; }
.list-row .label { flex:1; font:400 15px var(--font-ui); color:#1C1C1A; }
.list-row .chevron { width:18px; height:18px; color:#8C8C86; }   /* arrow → */
```
**Inbox/notification row** adds a thumbnail, multiline body, a green action link
("Track order" / "Shop now"), a timestamp in `--text-dim`, and an unread green
dot on the right.

### 6.12 Cards & Panels

```css
.panel { background:#FFFFFF; border:1px solid #E2E2DD; border-radius:12px; }
.panel-header { padding:16px 20px; font:600 18px var(--font-display); color:#1C1C1A; border-bottom:1px solid #E2E2DD; }
.panel-body { padding:20px; }
.detail-row { display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid #F0F0EC; }
.detail-label { color:#5A5A55; font:400 14px var(--font-ui); }
.detail-value { color:#1C1C1A; font:600 14px var(--font-ui); text-align:right; }
```

### 6.13 Accordion (FAQ)

Light dividers, bold question, `+` / `–` toggle on the right.

```css
.accordion-item { border-bottom:1px solid #E2E2DD; }
.accordion-trigger {
  width:100%; display:flex; justify-content:space-between; align-items:center;
  padding:20px 0; background:none; border:none; cursor:pointer; text-align:left;
  font:600 16px var(--font-ui); color:#1C1C1A;
}
.accordion-trigger .toggle { font-size:22px; color:#1C1C1A; }   /* + → – */
.accordion-panel { padding:0 0 20px; font:400 15px/1.5 var(--font-ui); color:#5A5A55; }
```

### 6.14 Hero / Marketing Section

```css
.hero { background:#023F2F; color:#FFFFFF; padding:48px 24px; text-align:center; }
.hero h1 { font:700 28px/1.2 var(--font-display); }       /* "The Home of Fresh starts" */
.hero .sub { font:400 16px var(--font-ui); color:#C6CEC9; margin-top:8px; }
.hero .cta { margin-top:24px; }
```
Marketing headlines mix weights for emphasis (e.g. *The Home of **Fresh
starts***): regular serif + bold serif on the key word.

### 6.15 Bottom Sheet / Modal

```css
.scrim { position:fixed; inset:0; background:rgba(2,42,32,0.55); }
.sheet {
  background:#FFFFFF; border-radius:16px 16px 0 0;
  box-shadow:var(--shadow-sheet);
  padding:20px; animation:sheet-in var(--transition-base) var(--ease-out);
}
.sheet .grabber { width:36px; height:4px; border-radius:999px; background:#E2E2DD; margin:0 auto 16px; }
```

### 6.16 Global Footer (Web)

```css
.footer { background:#2F3A40; color:#FFFFFF; padding:48px 32px; }
.footer h4 { font:600 16px var(--font-ui); color:#FFFFFF; margin-bottom:16px; }
.footer a  { display:block; font:400 14px var(--font-ui); color:#FFFFFF; padding:8px 0; opacity:0.92; }
.footer a:hover { opacity:1; text-decoration:underline; }
.footer .payment-icon { background:#FFFFFF; border-radius:4px; padding:4px 6px; height:28px; }   /* Visa, Mastercard, PayPal, Klarna, Apple Pay, Creation */
.footer .social svg { width:36px; height:36px; color:#FFFFFF; }   /* Facebook, Pinterest, Instagram, YouTube */
```

---

## 7. Iconography

- **Style:** line icons, ~1.75px stroke, rounded joins, monochrome.
- **Sizes:** 18px (inline), 22px (nav/tab), 24px (top bar actions).
- **Colour:** `#1C1C1A` on light, `#FFFFFF` on forest/slate, `#0A8A00` when
  active, `#8C8C86` for chevrons/metadata.
- **Favourite:** outline heart → fills `#0A8A00` (some surfaces use a solid
  black heart on the white circle when unselected).
- **Notification/basket count:** red `#D8232A` circle, white number, top-right.
- Recurring set: home, shop/tag, heart, store pin, person, search, mic, basket,
  share, mail, back-arrow, chevron-right, plus/minus.

---

## 8. Imagery

- **Warm, lived-in lifestyle photography** is the brand's primary visual asset —
  styled rooms, natural light, seasonal palettes. Products shown in context.
- Product thumbnails on a neutral `#F6F6F3` ground; square (1:1) aspect.
- Hero imagery may overlay serif headlines in white with a subtle dark gradient
  for legibility.
- Avoid stocky, cold, or clinical imagery — Dunelm reads as a real home.

---

## 9. Accessibility

- Target **WCAG 2.1 AA**. Body text meets ≥ 4.5:1; large text ≥ 3:1.
- `--green-action #0A8A00` on white passes AA for normal text (≈4.6:1). For
  green text on green or green on light-green, switch to `--green-link #0A7A0A`
  or add weight/size.
- **Never rely on colour alone** — pair status with icon + text (e.g. stock
  state, errors). Sale uses red + the word "Sale".
- Focus state: `outline: 2px solid #0A8A00; outline-offset: 2px;` plus the green
  focus ring shadow on inputs.
- Tap targets ≥ 44×44px (buttons here are 44–52px).
- Form errors: red border + descriptive message text, never colour only.

---

## 10. Page Layout

### Mobile App
```
┌─────────────────────────────┐  ← Top bar 56px (white): search pill · basket
├─────────────────────────────┤
│   Content (bg #E9EAE5)       │
│   ┌───────────────────────┐  │
│   │ Card / product grid    │  │  ← white cards, radius 12, soft shadow
│   │ (white surfaces)       │  │
│   └───────────────────────┘  │
├─────────────────────────────┤
│ Home · Shop · ♥ · Stores · ◌ │  ← Bottom tab bar 64px (active = green)
└─────────────────────────────┘
```

### Web
```
┌──────────────────────────────────────────────┐ ← Nav 56px (white) + sale strip
├────────────┬─────────────────────────────────┤
│ Filter     │  Results header (count · sort)   │
│ sidebar    │  ┌────┐ ┌────┐ ┌────┐ ┌────┐     │
│ (260px,    │  │card│ │card│ │card│ │card│  …  │  ← responsive product grid
│  white)    │  └────┘ └────┘ └────┘ └────┘     │
├────────────┴─────────────────────────────────┤
│  Footer (slate #2F3A40, multi-column)         │
└──────────────────────────────────────────────┘
```

---

## 11. Logo Usage

- **Wordmark:** "Dunelm" in the bespoke rounded sans, with the **house-roof
  motif** over the "lm". Always use the official asset — do not re-typeset.
- **Lockup:** "Dunelm" above the strapline "The **Home** of Homes" (with "Home"
  bolded).
- **Colour:** green `#0A8A00` on light backgrounds; white on forest-green and
  photographic backgrounds.
- **Clear space:** minimum equal to the height of the house-roof motif on all
  sides.
- **Minimum size:** 24px height (icon/roof) · 96px width (full wordmark).
- Do not recolour to a non-brand hue, stretch, add effects, or place green
  wordmark on busy or low-contrast imagery.

---

## 12. Token Quick-Reference (CSS Custom Properties)

```css
:root {
  /* Brand */
  --green-action:#0A8A00; --green-action-hover:#067006; --green-link:#0A7A0A;
  --green-forest:#023F2F; --green-forest-deep:#012A20; --on-green:#FFFFFF;

  /* Surface */
  --bg-page:#E9EAE5; --bg-surface:#FFFFFF; --bg-surface-alt:#F6F6F3;
  --bg-hero:#023F2F; --bg-row-hover:#F4F5F1; --bg-overlay:rgba(2,42,32,0.55);

  /* Text */
  --text-primary:#1C1C1A; --text-secondary:#5A5A55; --text-dim:#8C8C86;
  --text-on-dark:#FFFFFF; --text-on-dark-muted:#C6CEC9; --text-link:#0A7A0A;
  --text-price:#1C1C1A; --text-price-was:#8C8C86;

  /* Lines */
  --border-light:#E2E2DD; --border-input:#C9C9C3; --border-strong:#1C1C1A;
  --border-focus:#0A8A00;

  /* Status */
  --sale-red:#D8232A; --sale-red-dark:#B41C22; --info-blue:#0066CC;
  --success:#0A8A00; --warning:#E08600; --error:#C62828; --star-rating:#F5B400;

  /* Footer */
  --slate-footer:#2F3A40; --slate-footer-text:#FFFFFF; --slate-footer-muted:#AEB7BC;

  /* Type */
  --font-display:'Lora',Georgia,serif;
  --font-ui:'Inter',-apple-system,BlinkMacSystemFont,'Segoe UI',Arial,sans-serif;

  /* Radius */
  --radius-sm:8px; --radius-md:12px; --radius-lg:16px; --radius-pill:999px; --radius-full:50%;

  /* Elevation */
  --shadow-card:0 1px 3px rgba(20,20,18,.06),0 2px 8px rgba(20,20,18,.05);
  --shadow-float:0 4px 16px rgba(20,20,18,.10);
  --shadow-sheet:0 -4px 24px rgba(2,42,32,.18);
  --shadow-modal:0 8px 40px rgba(2,42,32,.25);

  /* Motion */
  --transition-fast:.15s ease; --transition-base:.25s ease;
  --ease-out:cubic-bezier(.16,1,.3,1);

  /* Spacing */
  --sp-xs:4px; --sp-sm:8px; --sp-md:12px; --sp-lg:16px; --sp-xl:20px;
  --sp-2xl:24px; --sp-3xl:32px; --sp-4xl:48px;
}
```

---

## Appendix — How Dunelm differs from a dense enterprise system

| Dimension | Dunelm | (cf. dark industrial WMS) |
|---|---|---|
| Surface | Light-first, warm white | Dark-first |
| Accent | Bright green = action | Yellow on near-black |
| Shape | Pills, rounded cards | Crisp edges, small radius |
| Density | Generous, photo-led | Maximum info density |
| Shadow | Soft, warm | Flat / minimal |
| Type | Serif display + humanist sans | System sans only |
| Mood | Homely-premium, friendly | Precision-industrial |

*Built from supplied Dunelm app screenshots, the app landing PDF, and sampled
brand colours. Substitute fonts (`Lora`, `Inter`) stand in for Dunelm's licensed
brand faces — swap on deployment.*
