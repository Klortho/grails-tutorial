package org.chrismaloney

class Album {
    String title
    static hasMany = [songs:Song]
    
    String toString() {
        title
    }
}
