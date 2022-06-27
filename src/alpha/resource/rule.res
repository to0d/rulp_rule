
;****************************************************
; Common utilies
;****************************************************
(alias add-stmt ->)

(defun size-of ((?m model)) (return (size-of-model ?m)))

;****************************************************
; Constraint utilies
;****************************************************

(defun init_cst_rule ((?m model))

    (if (equal ?m::?cst-init true)
        (return))
            
    (add-rule "CST01" ?m::$cst$ 
        if  $cst_type$:'(?node ?index ?type1) $cst_type$:'(?node ?index ?type2) (not (equal ?type1 ?type2)) 
        do 
            (defvar ?msg (strcat "conflict-type-constraint:'(" ?node " " ?index " " ?type1 " " ?type2 ")"))
            (-> $invalid_constraint$:'(?msg))
    )
    
    (add-rule "CST02" ?m::$cst$ if $cst_type$:'(?node ?index ?type1) do (-> $cst_node$:'(?node type)))
    (add-rule "CST03" ?m::$cst$ if $cst_max$:'(?node ?index ?value)  do (-> $cst_node$:'(?node max)))
    (add-rule "CST04" ?m::$cst$ if $cst_min$:'(?node ?index ?value)  do (-> $cst_node$:'(?node min)))

    (setq ?m::?cst-init true)
)

(defun add_cst_constraint_type ((?m model) (?node string) ?index ?type)
    (init_cst_rule ?m)        
    (add-stmt ?m $cst_type$:'(?node ?index ?type))
    (query-stmt ?m::$cst$ '(?c) from $invalid_constraint$:'(?c) 
            do (remove-stmt ?m $cst_type$:'(?node ?index ?type)) 
               (throw add-constraint-fail (strcat "type-constraint:'(" ?node " " ?index " " ?type "), " ?c))  limit 1)
    (return (add-constraint ?m (to-named-list ?node '(?...)) (type ?type on (to-atom (+ "?" ?index)))))
)

(defun remove_cst_constraint_type ((?m model) (?node string) ?index ?type)
    (init_cst_rule ?m)
    (defvar ?drop-count (remove-stmt ?m $cst_type$:'(?node ?index ?type)))
    (if (= ?drop-count 0) (return '()))
    (return (remove-constraint ?m (to-named-list ?node '(?...)) (type ?type on (to-atom (+ "?" ?index)))))
)

(defun add_cst_constraint_max ((?m model) (?node string) ?index (?value int))
    (init_cst_rule ?m)        
    (add-stmt ?m $cst_max$:'(?node ?index ?value))
    (query-stmt ?m::$cst$ '(?c) from $invalid_constraint$:'(?c) 
            do (remove-stmt ?m $cst_max$:'(?node ?index ?value)) 
               (throw add-constraint-fail (strcat "max-constraint:'(" ?node " " ?index " " ?value "), " ?c))  limit 1)
    (return (add-constraint ?m (to-named-list ?node '(?...)) (max ?value on (to-atom (+ "?" ?index)))))
)

(defun remove_cst_constraint_max ((?m model) (?node string) ?index (?value int))
    (init_cst_rule ?m)        
    (defvar ?drop-count (remove-stmt ?m $cst_max$:'(?node ?index ?value)))
    (if (= ?drop-count 0) (return '()))
    (return (remove-constraint ?m (to-named-list ?node '(?...)) (max ?value on (to-atom (+ "?" ?index)))))
)

(defun add_cst_constraint_min ((?m model) (?node string) ?index (?value int))
    (init_cst_rule ?m)        
    (add-stmt ?m $cst_min$:'(?node ?index ?value))
    (query-stmt ?m::$cst$ '(?c) from $invalid_constraint$:'(?c) 
            do (remove-stmt ?m $cst_min$:'(?node ?index ?value)) 
               (throw add-constraint-fail (strcat "min-constraint:'(" ?node " " ?index " " ?value "), " ?c))  limit 1)
    (return (add-constraint ?m (to-named-list ?node '(?...)) (min ?value on (to-atom (+ "?" ?index)))))
)

(defun remove_cst_constraint_min ((?m model) (?node string) ?index (?value int))
    (init_cst_rule ?m)        
    (defvar ?drop-count (remove-stmt ?m $cst_min$:'(?node ?index ?value)))
    (if (= ?drop-count 0) (return '()))
    (return (remove-constraint ?m (to-named-list ?node '(?...)) (min ?value on (to-atom (+ "?" ?index)))))
)

(defun add_cst_constraint_one_of ((?m model) (?node string) ?index (?value list))
    (init_cst_rule ?m)        
    (add-stmt ?m $cst_one_of$:'(?node ?index ?value))
    (query-stmt ?m::$cst$ '(?c) from $invalid_constraint$:'(?c) 
            do (remove-stmt ?m $cst_one_of$:'(?node ?index ?value)) 
               (throw add-constraint-fail (strcat "one-of-constraint:'(" ?node " " ?index " " ?value "), " ?c))  limit 1)
    (return (add-constraint ?m (to-named-list ?node '(?...)) (one-of ?value on (to-atom (+ "?" ?index)))))
)

(defun remove_cst_constraint_one_of ((?m model) (?node string) ?index (?value list))
    (init_cst_rule ?m)        
    (defvar ?drop-count (remove-stmt ?m $cst_one_of$:'(?node ?index ?value)))
    (if (= ?drop-count 0) (return '()))
    (return (remove-constraint ?m (to-named-list ?node '(?...)) (one-of ?value on (to-atom (+ "?" ?index)))))
)



;;(defun add_cst_constraint_index_1 ((?m model) (?node string) ?index ?cst_type)
;;    (init_cst_rule ?m)        
;;    (add-stmt ?m $cst_index_1:'(?node ?index ?cst_type))
;;    (query-stmt ?m::$cst$ '(?r ?c1 ?c2) from $invalid_constraint$:'(?r ?c1 ?c2) 
;;            do (remove-stmt ?m $cst_type$:'(?node ?index ?type)) 
;;               (throw add-constraint-fail (+ ?r ?c1 ?c2))
;;    limit 1)
;;)

