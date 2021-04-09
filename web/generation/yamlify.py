import yaml

from goals import GOALS

goals = {}
item_triggers = {}


class MyDumper(yaml.SafeDumper):
    # HACK: insert blank lines between top-level objects
    # inspired by https://stackoverflow.com/a/44284819/3786245
    def write_line_break(self, data=None):
        super().write_line_break(data)

        if len(self.indents) == 2:
            super().write_line_break()


for g in GOALS:
    if g.id in goals:
        raise Exception(f"Duplicate goal ID {g.id}")

    d = {}
    d['difficulty'] = g.difficulty
    if g.weight != 1.0:
        d['weight'] = g.weight
    if g.type != 'default':
        d['type'] = g.type
    for v, (mn, mx) in g.variable_ranges.items():
        d[v] = f'{mn}..{mx}'
    d['text'] = g.description_template
    if g.tooltip_template:
        d['tooltip'] = g.tooltip_template
    if g.antisynergy:
        d['antisynergy'] = g.antisynergy

    goals[g.id] = d

    if g.triggers_xml:
        trg = {}
        if len(g.triggers_xml) > 1:
            print(g.triggers_xml)
        for t in g.triggers_xml:
            needed = t.get('needed')
            if needed:
                if needed.startswith('$'):
                    trg['unique'] = needed
                else:
                    trg['unique'] = int(needed)

            quant = t.find('Quantity').text if t.find('Quantity') is not None else None
            if quant:
                if quant.startswith('$'):
                    trg['total'] = quant
                else:
                    trg['total'] = int(quant)

            names = []
            for n in t.iterfind('Name'):
                names.append(n.text)
            if len(names) > 1:
                trg['name'] = names
            elif len(names) == 1:
                trg['name'] = names[0]

            if t.find('ItemMatchGroup'):
                trg['TODO'] = 'MatchGroup'

        item_triggers[g.id] = trg


with open('goals.yml', 'w') as f:
    yaml.dump({'goals': goals}, f, Dumper=MyDumper, sort_keys=False)
    print(f"{len(goals)} goals")
with open('new_triggers.yml', 'w') as f:
    yaml.dump({'item_triggers': item_triggers}, f, Dumper=MyDumper, sort_keys=False)
    print(f"{len(item_triggers)} item triggers")
