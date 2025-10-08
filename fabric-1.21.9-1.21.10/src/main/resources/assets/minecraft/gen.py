"""_summary_

Create a python script that will scan a given minecraft assets folder

GOAL of the script: create textures_1.txt et and models_1.txt files for Dynmap containing pacthes and textures/models definitions based on
the content of a given minecraft assets folder.

Step 1:
Scan in the given minecrtaft/assets folder all files in minecraft\textures\block

for each file found will check if a json file with the same name exists in minecraft\models\block
if so, will parse the json file to find the texture definition
Exemple: (for oak_shelf.png will check if oak_shelf.json exists in models/block)
{
  "parent": "minecraft:block/template_shelf_body",
  "textures": {
    "all": "minecraft:block/oak_shelf",
    "particle": "minecraft:block/stripped_oak_log"
  }
} He has a parent, so will check if template_shelf_body.json exists in models/block
if so, will parse the json file to find the texture definition

Goal is to have in textures_1.txt file:
    only block, or texture definitions that are not part of an item model
    Exemple:
        block:id=jungle_fence,patch0-2=0:jungle_planks,transparency=TRANSPARENT
        block:id=%pale_oak_trapdoor,state=facing:north/half:top/open:true,patch0=0:pale_oak_trapdoor,transparency=SEMITRANSPARENT,stdrot=true
        block:id=%pale_oak_trapdoor,state=facing:north/half:top/open:false,patch0=0:pale_oak_trapdoor,transparency=SEMITRANSPARENT,stdrot=true
        block:id=%pale_oak_trapdoor,state=facing:north/half:bottom/open:true,patch0=0:pale_oak_trapdoor,transparency=SEMITRANSPARENT,stdrot=true
        block:id=%pale_oak_trapdoor,state=facing:north/half:bottom/open:false,patch0=0:pale_oak_trapdoor,transparency=SEMITRANSPARENT,stdrot=true
        block:id=%pale_oak_trapdoor,state=facing:south/half:top/open:true,patch0=0:pale_oak_trapdoor,transparency=SEMITRANSPARENT,stdrot=true
        block:id=%pale_oak_trapdoor,state=facing:south/half:top/open:false,patch0=0:pale_oak_trapdoor,transparency=SEMITRANSPARENT,stdrot=true
        block:id=%pale_oak_trapdoor,state=facing:south/half:bottom/open:true,patch0=0:pale_oak_trapdoor,transparency=SEMITRANSPARENT,stdrot=true
        block:id=%pale_oak_trapdoor,state=facing:south/half:bottom/open:false,patch0=0:pale_oak_trapdoor,transparency=SEMITRANSPARENT,stdrot=true
        block:id=%pale_oak_trapdoor,state=facing:west/half:top/open:true,patch0=0:pale_oak_trapdoor,transparency=SEMITRANSPARENT,stdrot=true
        block:id=%pale_oak_trapdoor,state=facing:west/half:top/open:false,patch0=0:pale_oak_trapdoor,transparency=SEMITRANSPARENT,stdrot=true
        texture:id=dirt_path_top
        texture:id=dirt_path_side
        texture:id=deepslate
        texture:id=deepslate_top
        texture:id=deepslate_gold_ore
        texture:id=deepslate_iron_ore
        texture:id=deepslate_coal_ore
        texture:id=deepslate_lapis_ore
        texture:id=deepslate_diamond_ore
        texture:id=deepslate_redstone_ore

In models_1.txt file:
    Multiple definition types can be used:
    modellist,boxblock,patchblock, customblock (avoid using cutomblock as much as possible, beceause we need to define a class for it)

    Exemple:
        customblock:id=cobbled_deepslate_stairs,id=deepslate_brick_stairs,id=deepslate_tile_stairs,id=polished_deepslate_stairs,id=cut_copper_stairs,id=oxidized_cut_copper_stairs,id=exposed_cut_copper_stairs,id=weathered_cut_copper_stairs,,id=waxed_cut_copper_stairs,id=waxed_oxidized_cut_copper_stairs,id=waxed_exposed_cut_copper_stairs,id=waxed_weathered_cut_copper_stairs,class=org.dynmap.hdmap.renderer.StairStateRenderer
        boxblock:id=moss_carpet,ymax=0.0625
        patchblock:id=cave_vines,id=cave_vines_plant,id=hanging_roots,patch0=VertX1Z0ToX0Z1,patch1=VertX1Z0ToX0Z1@90
        modellist:id=%candle,state=candles:1/lit:true,box=7.000000/0.000000/7.000000:9.000000/6.000000/9.000000:n/0/0.000000/8.000000/2.000000/14.000000:e/0/0.000000/8.000000/2.000000/14.000000:w/0/0.000000/8.000000/2.000000/14.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/14.000000,box=7.500000/6.000000/8.000000:8.500000/7.000000/8.000000/0.000000/45.000000/0.000000/8.000000/6.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000,box=7.500000/6.000000/8.000000:8.500000/7.000000/8.000000/0.000000/-45.000000/0.000000/8.000000/6.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000
        modellist:id=%candle,state=candles:1/lit:false,box=7.000000/0.000000/7.000000:9.000000/6.000000/9.000000:n/0/0.000000/8.000000/2.000000/14.000000:e/0/0.000000/8.000000/2.000000/14.000000:w/0/0.000000/8.000000/2.000000/14.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/14.000000,box=7.500000/6.000000/8.000000:8.500000/7.000000/8.000000/0.000000/45.000000/0.000000/8.000000/6.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000,box=7.500000/6.000000/8.000000:8.500000/7.000000/8.000000/0.000000/-45.000000/0.000000/8.000000/6.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000
        modellist:id=%candle,state=candles:2/lit:true,box=5.000000/0.000000/7.000000:7.000000/5.000000/9.000000:n/0/0.000000/8.000000/2.000000/13.000000:e/0/0.000000/8.000000/2.000000/13.000000:w/0/0.000000/8.000000/2.000000/13.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/13.000000,box=5.500000/5.000000/8.000000:6.500000/6.000000/8.000000/0.000000/45.000000/0.000000/6.000000/5.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=5.500000/5.000000/8.000000:6.500000/6.000000/8.000000/0.000000/-45.000000/0.000000/6.000000/5.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=9.000000/0.000000/6.000000:11.000000/6.000000/8.000000:n/0/0.000000/8.000000/2.000000/14.000000:e/0/0.000000/8.000000/2.000000/14.000000:w/0/0.000000/8.000000/2.000000/14.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/14.000000,box=9.500000/6.000000/7.000000:10.500000/7.000000/7.000000/0.000000/45.000000/0.000000/10.000000/6.000000/7.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000,box=9.500000/6.000000/7.000000:10.500000/7.000000/7.000000/0.000000/-45.000000/0.000000/10.000000/6.000000/7.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000
        modellist:id=%candle,state=candles:2/lit:false,box=5.000000/0.000000/7.000000:7.000000/5.000000/9.000000:n/0/0.000000/8.000000/2.000000/13.000000:e/0/0.000000/8.000000/2.000000/13.000000:w/0/0.000000/8.000000/2.000000/13.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/13.000000,box=5.500000/5.000000/8.000000:6.500000/6.000000/8.000000/0.000000/45.000000/0.000000/6.000000/5.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=5.500000/5.000000/8.000000:6.500000/6.000000/8.000000/0.000000/-45.000000/0.000000/6.000000/5.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=9.000000/0.000000/6.000000:11.000000/6.000000/8.000000:n/0/0.000000/8.000000/2.000000/14.000000:e/0/0.000000/8.000000/2.000000/14.000000:w/0/0.000000/8.000000/2.000000/14.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/14.000000,box=9.500000/6.000000/7.000000:10.500000/7.000000/7.000000/0.000000/45.000000/0.000000/10.000000/6.000000/7.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000,box=9.500000/6.000000/7.000000:10.500000/7.000000/7.000000/0.000000/-45.000000/0.000000/10.000000/6.000000/7.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000
        modellist:id=%candle,state=candles:3/lit:true,box=7.000000/0.000000/9.000000:9.000000/3.000000/11.000000:n/0/0.000000/8.000000/2.000000/11.000000:e/0/0.000000/8.000000/2.000000/11.000000:w/0/0.000000/8.000000/2.000000/11.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/11.000000,box=7.500000/3.000000/10.000000:8.500000/4.000000/10.000000/0.000000/45.000000/0.000000/8.000000/3.000000/10.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=7.500000/3.000000/10.000000:8.500000/4.000000/10.000000/0.000000/-45.000000/0.000000/8.000000/3.000000/10.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=5.000000/0.000000/7.000000:7.000000/5.000000/9.000000:n/0/0.000000/8.000000/2.000000/13.000000:e/0/0.000000/8.000000/2.000000/13.000000:w/0/0.000000/8.000000/2.000000/13.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/13.000000,box=5.500000/5.000000/8.000000:6.500000/6.000000/8.000000/0.000000/45.000000/0.000000/6.000000/5.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=5.500000/5.000000/8.000000:6.500000/6.000000/8.000000/0.000000/-45.000000/0.000000/6.000000/5.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=8.000000/0.000000/6.000000:10.000000/6.000000/8.000000:n/0/0.000000/8.000000/2.000000/14.000000:e/0/0.000000/8.000000/2.000000/14.000000:w/0/0.000000/8.000000/2.000000/14.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/14.000000,box=8.500000/6.000000/7.000000:9.500000/7.000000/7.000000/0.000000/45.000000/0.000000/9.000000/6.000000/7.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000,box=8.500000/6.000000/7.000000:9.500000/7.000000/7.000000/0.000000/-45.000000/0.000000/9.000000/6.000000/7.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000
        modellist:id=%candle,state=candles:3/lit:false,box=7.000000/0.000000/9.000000:9.000000/3.000000/11.000000:n/0/0.000000/8.000000/2.000000/11.000000:e/0/0.000000/8.000000/2.000000/11.000000:w/0/0.000000/8.000000/2.000000/11.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/11.000000,box=7.500000/3.000000/10.000000:8.500000/4.000000/10.000000/0.000000/45.000000/0.000000/8.000000/3.000000/10.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=7.500000/3.000000/10.000000:8.500000/4.000000/10.000000/0.000000/-45.000000/0.000000/8.000000/3.000000/10.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=5.000000/0.000000/7.000000:7.000000/5.000000/9.000000:n/0/0.000000/8.000000/2.000000/13.000000:e/0/0.000000/8.000000/2.000000/13.000000:w/0/0.000000/8.000000/2.000000/13.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/13.000000,box=5.500000/5.000000/8.000000:6.500000/6.000000/8.000000/0.000000/45.000000/0.000000/6.000000/5.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=5.500000/5.000000/8.000000:6.500000/6.000000/8.000000/0.000000/-45.000000/0.000000/6.000000/5.000000/8.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=8.000000/0.000000/6.000000:10.000000/6.000000/8.000000:n/0/0.000000/8.000000/2.000000/14.000000:e/0/0.000000/8.000000/2.000000/14.000000:w/0/0.000000/8.000000/2.000000/14.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/14.000000,box=8.500000/6.000000/7.000000:9.500000/7.000000/7.000000/0.000000/45.000000/0.000000/9.000000/6.000000/7.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000,box=8.500000/6.000000/7.000000:9.500000/7.000000/7.000000/0.000000/-45.000000/0.000000/9.000000/6.000000/7.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000
        modellist:id=%candle,state=candles:4/lit:true,box=6.000000/0.000000/8.000000:8.000000/3.000000/10.000000:n/0/0.000000/8.000000/2.000000/11.000000:e/0/0.000000/8.000000/2.000000/11.000000:w/0/0.000000/8.000000/2.000000/11.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/11.000000,box=6.500000/3.000000/9.000000:7.500000/4.000000/9.000000/0.000000/45.000000/0.000000/7.000000/3.000000/9.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=6.500000/3.000000/9.000000:7.500000/4.000000/9.000000/0.000000/-45.000000/0.000000/7.000000/3.000000/9.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=9.000000/0.000000/8.000000:11.000000/5.000000/10.000000:n/0/0.000000/8.000000/2.000000/13.000000:e/0/0.000000/8.000000/2.000000/13.000000:w/0/0.000000/8.000000/2.000000/13.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/13.000000,box=9.500000/5.000000/9.000000:10.500000/6.000000/9.000000/0.000000/45.000000/0.000000/10.000000/5.000000/9.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=9.500000/5.000000/9.000000:10.500000/6.000000/9.000000/0.000000/-45.000000/0.000000/10.000000/5.000000/9.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=5.000000/0.000000/5.000000:7.000000/5.000000/7.000000:n/0/0.000000/8.000000/2.000000/13.000000:e/0/0.000000/8.000000/2.000000/13.000000:w/0/0.000000/8.000000/2.000000/13.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/13.000000,box=5.500000/5.000000/6.000000:6.500000/6.000000/6.000000/0.000000/45.000000/0.000000/6.000000/5.000000/6.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=5.500000/5.000000/6.000000:6.500000/6.000000/6.000000/0.000000/-45.000000/0.000000/6.000000/5.000000/6.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=8.000000/0.000000/5.000000:10.000000/6.000000/7.000000:n/0/0.000000/8.000000/2.000000/14.000000:e/0/0.000000/8.000000/2.000000/14.000000:w/0/0.000000/8.000000/2.000000/14.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/14.000000,box=8.500000/6.000000/6.000000:9.500000/7.000000/6.000000/0.000000/45.000000/0.000000/9.000000/6.000000/6.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000,box=8.500000/6.000000/6.000000:9.500000/7.000000/6.000000/0.000000/-45.000000/0.000000/9.000000/6.000000/6.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000
        modellist:id=%candle,state=candles:4/lit:false,box=6.000000/0.000000/8.000000:8.000000/3.000000/10.000000:n/0/0.000000/8.000000/2.000000/11.000000:e/0/0.000000/8.000000/2.000000/11.000000:w/0/0.000000/8.000000/2.000000/11.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/11.000000,box=6.500000/3.000000/9.000000:7.500000/4.000000/9.000000/0.000000/45.000000/0.000000/7.000000/3.000000/9.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=6.500000/3.000000/9.000000:7.500000/4.000000/9.000000/0.000000/-45.000000/0.000000/7.000000/3.000000/9.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=9.000000/0.000000/8.000000:11.000000/5.000000/10.000000:n/0/0.000000/8.000000/2.000000/13.000000:e/0/0.000000/8.000000/2.000000/13.000000:w/0/0.000000/8.000000/2.000000/13.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/13.000000,box=9.500000/5.000000/9.000000:10.500000/6.000000/9.000000/0.000000/45.000000/0.000000/10.000000/5.000000/9.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=9.500000/5.000000/9.000000:10.500000/6.000000/9.000000/0.000000/-45.000000/0.000000/10.000000/5.000000/9.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=5.000000/0.000000/5.000000:7.000000/5.000000/7.000000:n/0/0.000000/8.000000/2.000000/13.000000:e/0/0.000000/8.000000/2.000000/13.000000:w/0/0.000000/8.000000/2.000000/13.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/13.000000,box=5.500000/5.000000/6.000000:6.500000/6.000000/6.000000/0.000000/45.000000/0.000000/6.000000/5.000000/6.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=5.500000/5.000000/6.000000:6.500000/6.000000/6.000000/0.000000/-45.000000/0.000000/6.000000/5.000000/6.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/1.000000/5.000000/0.000000/6.000000,box=8.000000/0.000000/5.000000:10.000000/6.000000/7.000000:n/0/0.000000/8.000000/2.000000/14.000000:e/0/0.000000/8.000000/2.000000/14.000000:w/0/0.000000/8.000000/2.000000/14.000000:d/0/0.000000/14.000000/2.000000/16.000000:u/0/0.000000/6.000000/2.000000/8.000000:s/0/0.000000/8.000000/2.000000/14.000000,box=8.500000/6.000000/6.000000:9.500000/7.000000/6.000000/0.000000/45.000000/0.000000/9.000000/6.000000/6.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000,box=8.500000/6.000000/6.000000:9.500000/7.000000/6.000000/0.000000/-45.000000/0.000000/9.000000/6.000000/6.000000:n/0/0.000000/5.000000/1.000000/6.000000:s/0/0.000000/5.000000/1.000000/6.000000  

All exemple shows what would be the content of a models file using this script.        
"""
import json
import os
from pathlib import Path
from typing import Dict, List, Set, Tuple, Optional

class MinecraftToDynmapConverter:
    def __init__(self, assets_path: str, version: str = "1.21.9"):
        self.assets_path = Path(assets_path)
        self.version = version
        self.blockstates_path = self.assets_path / "minecraft" / "blockstates"
        self.models_path = self.assets_path / "minecraft" / "models" / "block"
        self.textures_path = self.assets_path / "minecraft" / "textures" / "block"
        
        self.texture_lines: List[str] = []
        self.block_lines: List[str] = []
        self.modellist_lines: List[str] = []
        
        self.processed_textures: Set[str] = set()
        self.processed_blocks: Set[str] = set()
    
    def scan_and_generate(self, output_dir: str = "."):
        """Scanne les assets et génère les fichiers Dynmap"""
        print(f"Scanning assets in: {self.assets_path}")
        
        if not self.textures_path.exists():
            print(f"Error: Textures path not found: {self.textures_path}")
            return
        
        # Étape 1: Scanner toutes les textures de block
        texture_files = sorted(self.textures_path.glob("*.png"))
        print(f"Found {len(texture_files)} texture files")
        
        for texture_file in texture_files:
            texture_name = texture_file.stem
            self.process_texture(texture_name)
        
        # Étape 2: Scanner tous les blockstates
        if self.blockstates_path.exists():
            blockstate_files = sorted(self.blockstates_path.glob("*.json"))
            print(f"Found {len(blockstate_files)} blockstate files")
            
            for blockstate_file in blockstate_files:
                block_id = blockstate_file.stem
                print(f"Processing blockstate: {block_id}")
                
                try:
                    self.process_blockstate(block_id, blockstate_file)
                except Exception as e:
                    print(f"  Error processing {block_id}: {e}")
        
        # Écrire les fichiers de sortie
        self.write_output_files(output_dir)
        print(f"\nGeneration complete!")
        print(f"  Texture definitions: {len(self.texture_lines)}")
        print(f"  Block definitions: {len(self.block_lines)}")
        print(f"  Modellist definitions: {len(self.modellist_lines)}")
    
    def process_texture(self, texture_name: str):
        """Traite une texture et vérifie s'il y a un modèle correspondant"""
        if texture_name in self.processed_textures:
            return
        
        model_file = self.models_path / f"{texture_name}.json"
        
        # Si un modèle existe, on ne crée pas de définition texture simple
        # car il sera traité via les blockstates
        if not model_file.exists():
            # Texture simple sans modèle
            self.texture_lines.append(f"texture:id={texture_name}")
            self.processed_textures.add(texture_name)
    
    def process_blockstate(self, block_id: str, blockstate_file: Path):
        """Traite un fichier blockstate"""
        with open(blockstate_file, 'r', encoding='utf-8') as f:
            blockstate_data = json.load(f)
        
        # Gérer les différents formats de blockstate
        if "variants" in blockstate_data:
            self.process_variants(block_id, blockstate_data["variants"])
        elif "multipart" in blockstate_data:
            self.process_multipart(block_id, blockstate_data["multipart"])
    
    def process_variants(self, block_id: str, variants: Dict):
        """Traite les blockstates avec variants"""
        for variant_key, variant_data in variants.items():
            if isinstance(variant_data, list):
                variant_data = variant_data[0]
            
            model_name = variant_data.get("model", "").replace("minecraft:block/", "")
            if not model_name:
                continue
            
            # Extraire les rotations
            x_rot = variant_data.get("x", 0)
            y_rot = variant_data.get("y", 0)
            
            # Charger le modèle complet
            model_data = self.load_model_with_inheritance(model_name)
            
            if model_data:
                # Déterminer le type de définition à générer
                self.generate_definition(block_id, variant_key, model_data, x_rot, y_rot)
    
    def process_multipart(self, block_id: str, multipart: List):
        """Traite les blockstates avec multipart"""
        for part in multipart:
            when_conditions = part.get("when", {})
            apply_data = part.get("apply", {})
            
            if isinstance(apply_data, list):
                apply_data = apply_data[0]
            
            model_name = apply_data.get("model", "").replace("minecraft:block/", "")
            if not model_name:
                continue
            
            x_rot = apply_data.get("x", 0)
            y_rot = apply_data.get("y", 0)
            
            model_data = self.load_model_with_inheritance(model_name)
            
            if model_data:
                state_str = self.format_multipart_conditions(when_conditions)
                self.generate_definition(block_id, state_str, model_data, x_rot, y_rot)
    
    def generate_definition(self, block_id: str, state: str, model_data: Dict, x_rot: int, y_rot: int):
        """Génère la définition appropriée (block ou modellist)"""
        elements = model_data.get("elements", [])
        
        if not elements:
            # Pas d'éléments, peut-être un modèle parent simple
            return
        
        # Récupérer les textures
        textures = self.resolve_all_texture_references(model_data.get("textures", {}))
        main_texture = self.get_main_texture(textures)
        
        if main_texture:
            self.processed_textures.add(main_texture)
        
        # Déterminer le type de block
        block_type = self.determine_block_type(block_id, elements)
        
        state_str = self.format_state(state)
        transparency = self.determine_transparency(block_id)
        
        if block_type == "simple" and not state_str:
            # Block simple sans état
            key = f"{block_id}"
            if key not in self.processed_blocks:
                self.block_lines.append(
                    f"block:id={block_id},patch0=0:{main_texture},transparency={transparency}"
                )
                self.processed_blocks.add(key)
        
        elif block_type == "simple":
            # Block avec états mais géométrie simple
            key = f"{block_id}:{state_str}"
            if key not in self.processed_blocks:
                self.block_lines.append(
                    f"block:id=%{block_id},state={state_str},patch0=0:{main_texture},transparency={transparency},stdrot=true"
                )
                self.processed_blocks.add(key)
        
        else:
            # Block complexe nécessitant modellist
            boxes = self.parse_model_to_boxes(elements, textures, x_rot, y_rot)
            if boxes:
                modellist = self.generate_modellist(block_id, state_str, boxes)
                if modellist:
                    self.modellist_lines.append(modellist)
    
    def determine_block_type(self, block_id: str, elements: List[Dict]) -> str:
        """Détermine si un block est simple ou nécessite modellist"""
        # Si un seul élément qui est un cube complet, c'est simple
        if len(elements) == 1:
            elem = elements[0]
            from_pos = elem.get("from", [0, 0, 0])
            to_pos = elem.get("to", [16, 16, 16])
            
            if from_pos == [0, 0, 0] and to_pos == [16, 16, 16]:
                return "simple"
        
        # Sinon, nécessite modellist
        return "complex"
    
    def parse_model_to_boxes(self, elements: List[Dict], textures: Dict[str, str], 
                            x_rot: int, y_rot: int) -> List[Dict]:
        """Parse les éléments en boxes pour modellist"""
        boxes = []
        
        for element in elements:
            from_pos = element.get("from", [0, 0, 0])
            to_pos = element.get("to", [16, 16, 16])
            faces = element.get("faces", {})
            rotation = element.get("rotation")
            
            box = {
                "from": from_pos,
                "to": to_pos,
                "faces": {},
                "rotation": None
            }
            
            # Rotation de l'élément
            if rotation:
                origin = rotation.get("origin", [8, 8, 8])
                axis = rotation.get("axis", "y")
                angle = rotation.get("angle", 0)
                box["element_rotation"] = f"{angle}/{axis}/{origin[0]}/{origin[1]}/{origin[2]}"
            
            # Rotation du blockstate
            if x_rot != 0 or y_rot != 0:
                box["rotation"] = f"R/{x_rot}/{y_rot}/0"
            
            # Parser les faces
            face_map = {"north": "n", "south": "s", "east": "e", "west": "w", "up": "u", "down": "d"}
            
            for face_name, face_data in faces.items():
                if not face_data:
                    continue
                
                uv = face_data.get("uv", [0, 0, 16, 16])
                texture_ref = face_data.get("texture", "#all")
                
                # Résoudre la texture
                if texture_ref.startswith("#"):
                    tex_key = texture_ref[1:]
                    tex_name = textures.get(tex_key, "")
                else:
                    tex_name = texture_ref.replace("minecraft:block/", "").replace("minecraft:", "")
                
                if tex_name:
                    dynmap_face = face_map.get(face_name, face_name[0])
                    box["faces"][dynmap_face] = {"uv": uv}
            
            boxes.append(box)
        
        return boxes
    
    def generate_modellist(self, block_id: str, state: str, boxes: List[Dict]) -> str:
        """Génère une ligne modellist"""
        if not boxes:
            return ""
        
        state_part = f",state={state}" if state else ""
        
        box_parts = []
        for box in boxes:
            from_pos = box["from"]
            to_pos = box["to"]
            
            box_str = f"{from_pos[0]:.6f}/{from_pos[1]:.6f}/{from_pos[2]:.6f}:{to_pos[0]:.6f}/{to_pos[1]:.6f}/{to_pos[2]:.6f}"
            
            # Ajouter les faces dans l'ordre: n, d, w, e, s, u
            face_strs = []
            for face in ["n", "d", "w", "e", "s", "u"]:
                if face in box["faces"]:
                    uv = box["faces"][face]["uv"]
                    face_str = f"{face}/0/{uv[0]:.6f}/{uv[1]:.6f}/{uv[2]:.6f}/{uv[3]:.6f}"
                    face_strs.append(face_str)
            
            if face_strs:
                box_str += ":" + ":".join(face_strs)
                
                # Ajouter rotation si présente
                if box.get("rotation"):
                    box_str += ":" + box["rotation"]
                elif box.get("element_rotation"):
                    box_str += "/" + box["element_rotation"]
                
                box_parts.append(f"box={box_str}")
        
        if not box_parts:
            return ""
        
        return f"[{self.version}-]modellist:id=%{block_id}{state_part},{','.join(box_parts)}"
    
    def load_model_with_inheritance(self, model_name: str, depth: int = 0) -> Optional[Dict]:
        """Charge un modèle avec résolution de l'héritage"""
        if depth > 10:
            return None
        
        model_file = self.models_path / f"{model_name}.json"
        if not model_file.exists():
            return None
        
        try:
            with open(model_file, 'r', encoding='utf-8') as f:
                model_data = json.load(f)
            
            parent = model_data.get("parent", "")
            if parent:
                parent_name = parent.replace("minecraft:block/", "").replace("minecraft:", "").replace("block/", "")
                parent_data = self.load_model_with_inheritance(parent_name, depth + 1)
                
                if parent_data:
                    return self.merge_model_data(parent_data, model_data)
            
            return model_data
        except Exception as e:
            print(f"  Warning: Could not load model {model_name}: {e}")
            return None
    
    def merge_model_data(self, parent: Dict, child: Dict) -> Dict:
        """Fusionne parent et enfant"""
        merged = parent.copy()
        
        if "textures" in parent:
            merged["textures"] = parent["textures"].copy()
        if "textures" in child:
            if "textures" not in merged:
                merged["textures"] = {}
            merged["textures"].update(child["textures"])
        
        if "elements" in child:
            merged["elements"] = child["elements"]
        elif "elements" in parent:
            merged["elements"] = parent["elements"]
        
        for key in child:
            if key not in ["textures", "elements", "parent"]:
                merged[key] = child[key]
        
        return merged
    
    def resolve_all_texture_references(self, texture_refs: Dict, depth: int = 0) -> Dict[str, str]:
        """Résout toutes les références de textures"""
        if depth > 10:
            return texture_refs
        
        resolved = {}
        needs_resolution = False
        
        for key, value in texture_refs.items():
            if isinstance(value, str):
                if value.startswith("#"):
                    ref_key = value[1:]
                    if ref_key in texture_refs and not texture_refs[ref_key].startswith("#"):
                        resolved[key] = texture_refs[ref_key]
                    elif ref_key in texture_refs:
                        resolved[key] = texture_refs[ref_key]
                        needs_resolution = True
                    else:
                        resolved[key] = value
                else:
                    clean_value = value.replace("minecraft:block/", "").replace("minecraft:", "")
                    resolved[key] = clean_value
        
        if needs_resolution:
            return self.resolve_all_texture_references(resolved, depth + 1)
        
        return resolved
    
    def get_main_texture(self, textures: Dict[str, str]) -> str:
        """Récupère la texture principale"""
        for key in ["all", "texture", "particle"]:
            if key in textures:
                return textures[key]
        
        if textures:
            return list(textures.values())[0]
        
        return ""
    
    def format_state(self, variant_key: str) -> str:
        """Formate l'état"""
        if variant_key == "" or variant_key == "normal":
            return ""
        return variant_key.replace(",", "/")
    
    def format_multipart_conditions(self, conditions: Dict) -> str:
        """Formate les conditions multipart"""
        if not conditions:
            return ""
        
        if "AND" in conditions:
            parts = []
            for cond in conditions["AND"]:
                for key, value in cond.items():
                    parts.append(f"{key}:{value}")
            return "/".join(parts)
        
        if "OR" in conditions:
            first_cond = conditions["OR"][0]
            parts = []
            for key, value in first_cond.items():
                parts.append(f"{key}:{value}")
            return "/".join(parts)
        
        parts = []
        for key, value in conditions.items():
            if isinstance(value, str):
                parts.append(f"{key}:{value}")
        
        return "/".join(parts)
    
    def determine_transparency(self, block_id: str) -> str:
        """Détermine la transparence"""
        transparent = ["air", "water", "lava", "fire"]
        semitransparent = ["stairs", "slab", "fence", "wall", "door", "trapdoor", "glass", 
                          "pane", "leaves", "bars", "chain", "lantern", "shelf", "candle"]
        
        for kw in transparent:
            if kw in block_id:
                return "TRANSPARENT"
        
        for kw in semitransparent:
            if kw in block_id:
                return "SEMITRANSPARENT"
        
        return "OPAQUE"
    
    def write_output_files(self, output_dir: str):
        """Écrit les fichiers de sortie"""
        output_path = Path(output_dir)
        output_path.mkdir(exist_ok=True)
        
        # Écrire textures_1.txt
        textures_file = output_path / "textures_1.txt"
        with open(textures_file, 'w', encoding='utf-8') as f:
            for line in sorted(self.texture_lines):
                f.write(line + "\n")
        print(f"Written: {textures_file}")
        
        # Écrire models_1.txt
        models_file = output_path / "models_1.txt"
        with open(models_file, 'w', encoding='utf-8') as f:
            for line in sorted(self.block_lines):
                f.write(line + "\n")
            for line in sorted(self.modellist_lines):
                f.write(line + "\n")
        print(f"Written: {models_file}")


def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='Convert Minecraft assets to Dynmap format')
    parser.add_argument('assets_path', help='Path to Minecraft assets directory')
    parser.add_argument('-o', '--output', default='.', help='Output directory')
    parser.add_argument('-v', '--version', default='1.21.4', help='Minecraft version')
    
    args = parser.parse_args()
    
    converter = MinecraftToDynmapConverter(args.assets_path, args.version)
    converter.scan_and_generate(args.output)


if __name__ == "__main__":
    main()