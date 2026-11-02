---
title: Widget size and transparency
feature: home-widget
type: config
summary: Background is 0x99FFFFFF by day and 0x99121212 by night, about 60% opaque. Default size is 4x3 cells and resizable. Change the first two hex digits to adjust opacity.
keywords:
  - WidgetBackground
  - 0x99
  - ColorProvider
  - agent_widget_info
  - targetCellWidth
aliases:
  - opacity
  - see-through
  - semi-transparent
  - transparency
questions:
  - How do I change how transparent the widget is?
  - What size is the widget?
sources:
  - app/src/main/java/com/gaurav/devagent/widget/AgentWidget.kt
  - app/src/main/res/xml/agent_widget_info.xml
---

# Widget size and transparency

## Widget transparency setting

`WidgetBackground` in `widget/AgentWidget.kt` sets the colors: `0x99FFFFFF` in light mode and `0x99121212` in dark mode. The first two hex digits are the opacity, so `66` is about 40%, `99` about 60% and `CC` about 80%. Edit them and run `./build.sh`. It was made semi-transparent on 2026-10-26, at the user's request, so the wallpaper shows through while text stays readable.

## Widget size settings

`res/xml/agent_widget_info.xml` sets a default of 4 by 3 cells, a minimum of 250 by 180 dp, and resizing down to 180 by 110 dp in both directions.
