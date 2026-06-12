[![License](http://img.shields.io/:license-apache-blue.svg)](LICENSE)

# LSVG

A lean Android SVG library with elements manipulation capabilities.

# Goals
- support a reasonable subset of the SVG specification so as to be able to
display most drawings made with popular vector graphics drawing tools
- allow some modifications of the SVG tree, and update the display accordingly
- keep a small footprint and acceptable performances

# Features

## Rendering
### Parsed elements
LSVG will parse `svg, g, path, rect, ellipse, linearGradient, radialGradient, stop` and `defs` elements.
Everything else, most notably text and filters, is not supported.

Basically LSVG converts all shapes to paths. As a consequence rendering mainly consists in rendering
paths inside groups.

### Paths
All path operations and encoding should be supported. Working with path should bring no big surprise.
At runtime, rect and ellipse elements are converted to path.

### Transforms
Raw matrices are supported, as well as combinations of `translate, scale, skewX, skewY` and `rotate` operations.

### Styles
Style descriptions can include:
`fill, stroke, stroke-width, stroke-linecap, stroke-linejoin, stroke-miterlimit, stroke-dasharray,
stroke-dashoffset, stroke-opacity, fill-opacity` and `display`.

Fill and stroke can refer to colors (as parsed by Android Color.parse()) or gradients.
Linear and radial gradients are supported, with transformations.

## Interaction
Rendering is not the sole purpose of LSVG. The lib allows basic modifications of the tree, such as
colors, shapes, transformations and visibility. The available operations are currently limited though.

Some form of interactivity is also enabled through picking: one can retrieve the path at a given
position.

## Android integration
LSVG comes with two entry points: a flexible SvgDrawable and a SvgDrawableView, ready to use in your
xml layout files.
Drawings can be loaded from assets, resources or files.

## Use in your project
Add `net.pierrox.android:lsvg:1.0` to your dependencies in your build.gradle file, like this:

```
dependencies {
    compile 'net.pierrox.android:lsvg:1.0'
}
```

Make sure that `jcenter()` is listed in your repositories.

## License
*licensed under the [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0)*.

LSVG uses code from:
- Pixplicity / sharp [https://github.com/Pixplicity/sharp/](https://github.com/Pixplicity/sharp/) (path parsing)
- Batik [https://xmlgraphics.apache.org/batik/](https://xmlgraphics.apache.org/batik/) (path parsing used by sharp)

## Use
LSVG is in use in these apps:
- [Lightning Launcher](https://play.google.com/store/apps/details?id=net.pierrox.lightning_launcher_extreme)
- [Kids Games](https://play.google.com/store/apps/details?id=net.pierrox.baby_games)
