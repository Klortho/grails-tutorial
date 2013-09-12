package org.chrismaloney

import org.springframework.dao.DataIntegrityViolationException

class AlbumController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    // The index action is the default.  This is executed if no action is specified
    // in the URI controller (whereever that is).
    // This simply redirects to the list action, passing along any parameters.
    def index() {
        redirect(action: "list", params: params)
    }

    // The list action provides a list of all albums.  It delegates to the static
    // list method of the Album class to obtain a java.util.List of Album instances.
    // That static list method is provided by the GORM.
    // 'max' is pulled from a query string param: i.e. /list?max=20
    // Note the '?:' operator here:  nice!
    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)

        // The last thing in the method is what is returned, and the "[]" mean that it
        // is a map.  This is the context variable for the template.
        [albumInstanceList: Album.list(params), albumInstanceTotal: Album.count()]
    }

    // This shows details about a given album.  Again, the Album.get() method is
    // provided by the GORM.
    // Look at what happens if the album is not found.  You get redirected to the list,
    // but a message will display saying "Album not found with id xx".  This means it must
    // be stateful.  See the 'flash' controller:
    // http://grails.org/doc/2.2.4/ref/Controllers/flash.html.
    // Notice how internationalization is done, with message codes and defaults.
    // Question:  how does the program know that in this case, the 'id' parameter is a path
    // segment rather than a query-string param?
    def show(Long id) {
        def albumInstance = Album.get(id)
        if (!albumInstance) {
            flash.message = message(code: 'default.not.found.message', 
                                    args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "list")
            return
        }

        [albumInstance: albumInstance]
    }

    // This puts up the "create a new album" form.  So, note that you can pre-populate
    // the form with the album title by going to the URL, for example,
    // /album/create?title=foo.
    // I guess that the "new" here creates a new Album object, but because the save()
    // method is never called on it, it does not persist.
    def create() {
        [albumInstance: new Album(params)]
    }

    // This can only be POSTed to.  The create action POSTs here, to save the newly
    // created object.
    def save() {
        def albumInstance = new Album(params)
        if (!albumInstance.save(flush: true)) {
            render(view: "create", model: [albumInstance: albumInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'album.label', default: 'Album'), albumInstance.id])
        redirect(action: "show", id: albumInstance.id)
    }

    // The edit action displays a form that let's the user edit the object.  The code
    // here is identical to the show action, but it delegates to a different view (the
    // edit view, of course).
    def edit(Long id) {
        def albumInstance = Album.get(id)
        if (!albumInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "list")
            return
        }

        [albumInstance: albumInstance]
    }

    // The update action only accepts POST, and it gets POSTed to by edit.
    def update(Long id, Long version) {
        def albumInstance = Album.get(id)
        if (!albumInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (albumInstance.version > version) {
                albumInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'album.label', default: 'Album')] as Object[],
                          "Another user has updated this Album while you were editing")
                render(view: "edit", model: [albumInstance: albumInstance])
                return
            }
        }

        // This sets all of the album's properties equal to the values provided by the
        // posted form parameters.
        albumInstance.properties = params

        if (!albumInstance.save(flush: true)) {
            render(view: "edit", model: [albumInstance: albumInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'album.label', default: 'Album'), albumInstance.id])
        redirect(action: "show", id: albumInstance.id)
    }

    // You are only allowed to POST to this action (see allowedMethods above).
    // It first checks to see if the action exists.
    def delete(Long id) {
        def albumInstance = Album.get(id)
        if (!albumInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "list")
            return
        }

        try {
            albumInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "show", id: id)
        }
    }
}
