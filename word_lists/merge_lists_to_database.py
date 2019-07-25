#! /usr/bin/env python3


import sys
import os.path
import sqlite3
import re
import shutil


# word categories
WORDSET_CATEGORY_NONE = 0;
WORDSET_CATEGORY_NATURE = 1;
WORDSET_CATEGORY_OBJECTS = 2;
WORDSET_CATEGORY_STRUCTURES = 4;
WORDSET_CATEGORY_VEHICLES = 8;
WORDSET_CATEGORY_ALL = ~0;


# convert line to word and properties
def convert_line_to_word_properties(line):
	line_split = line.split('#')
	if len(line_split) < 3:
		return
	word = line_split[0][:-1]
	if len(word) <= 0:
		return
	language = re.search('[a-z]+', line_split[1]).group()
	difficulty = int(re.search('[0-9]+', line_split[2]).group())
	return [word, language, difficulty]

# checks the file for wrong contents
def check_word_list_file(lines):
	for line in lines:
		regex_match = '[^a-zA-Z#\n 124äëïöüÄËÏÖÜß\-()]+'
		if re.search(regex_match, line, re.UNICODE):
			return False
	return True


# read from database text files and associate with categoties
print("reading word lists ...")
words = []
database_files = [
	['word_list_nature.txt', WORDSET_CATEGORY_NATURE],
	['word_list_objects.txt', WORDSET_CATEGORY_OBJECTS],
	['word_list_structures.txt', WORDSET_CATEGORY_STRUCTURES],
	['word_list_vehicles.txt', WORDSET_CATEGORY_VEHICLES]
]
for file in database_files:
	with open(file[0], 'r') as f:
		lines = f.readlines()
		if not check_word_list_file(lines):
			print('WARNING: wrong contents in ' + file[0] + ', ignoring this file')
			continue
		word_category = file[1]
		# extract word and properties from the read line.
		add_words = [convert_line_to_word_properties(line) for line in lines]	
		add_words = [word + [word_category] for word in add_words if word != None]
		# add to words list.
		words += add_words


# create merged database
print("creating database ...")
if os.path.isfile('initial_wordset_database_merged.db'):
	os.remove('initial_wordset_database_merged.db')
conn = sqlite3.connect('initial_wordset_database_merged.db')

conn.execute('''CREATE TABLE android_metadata (locale TEXT)''')
conn.execute('''INSERT INTO android_metadata (locale) values('en_US')''')
conn.execute('''COMMIT''')

conn.execute('''CREATE TABLE "wordset" (_id INTEGER PRIMARY KEY, language TEXT, word TEXT, difficulty INTEGER, category INTEGER)''')
id = 1
for word in words:
	conn.execute('''INSERT INTO wordset (_id, language, word, difficulty, category) values(%d, "%s", "%s", %d, %d)''' % (id, word[1], word[0], word[2], word[3]))
	conn.execute('''COMMIT''')
	id = id + 1


# copy database to raw folder
print("copying database ...")
db_path = '../app/src/main/res/raw/initial_wordset_database.db'
if os.path.isfile(db_path):
	os.remove(db_path)
shutil.copyfile('initial_wordset_database_merged.db', db_path)


# finished
print("finished!")




