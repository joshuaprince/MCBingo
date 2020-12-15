# Generated by Django 3.1.3 on 2020-12-15 04:22

from django.db import migrations, models
import django.db.models.deletion
import generation.difficulty


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Board',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('game_code', models.SlugField(max_length=128, unique=True)),
                ('seed', models.SlugField(max_length=128)),
                ('difficulty', models.IntegerField(choices=[(generation.difficulty.Difficulty['VERY_EASY'], 0), (generation.difficulty.Difficulty['EASY'], 1), (generation.difficulty.Difficulty['MEDIUM'], 2), (generation.difficulty.Difficulty['HARD'], 3), (generation.difficulty.Difficulty['VERY_HARD'], 4)])),
            ],
        ),
        migrations.CreateModel(
            name='Square',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('position', models.IntegerField()),
                ('text', models.CharField(max_length=256)),
                ('board', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='backend.board')),
            ],
            options={
                'unique_together': {('board', 'position')},
            },
        ),
        migrations.CreateModel(
            name='PlayerBoard',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('player_name', models.CharField(max_length=128)),
                ('squares', models.CharField(default='0000000000000000000000000', max_length=25)),
                ('disconnected_at', models.DateTimeField(null=True)),
                ('board', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='backend.board')),
            ],
            options={
                'unique_together': {('board', 'player_name')},
            },
        ),
    ]
